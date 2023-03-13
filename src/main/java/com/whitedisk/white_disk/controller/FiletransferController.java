package com.whitedisk.white_disk.controller;

import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.MimeUtils;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.qiwenshare.ufop.factory.UFOPFactory;
import com.qiwenshare.ufop.operation.download.Downloader;
import com.qiwenshare.ufop.operation.download.domain.DownloadFile;
import com.qiwenshare.ufop.operation.download.domain.Range;
import com.qiwenshare.ufop.util.UFOPUtils;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.BatchDownloadFileDTO;
import com.whitedisk.white_disk.dto.file.DownloadFileDTO;
import com.whitedisk.white_disk.dto.file.PreviewDTO;
import com.whitedisk.white_disk.dto.file.UploadFileDTO;
import com.whitedisk.white_disk.entity.FileEntity;
import com.whitedisk.white_disk.entity.StorageEntity;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.service.api.IFileService;
import com.whitedisk.white_disk.service.api.IFileTransferService;
import com.whitedisk.white_disk.service.api.IStorageService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.vo.file.UploadFileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.PostRemove;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author white
 */
@Slf4j
@RestController
@RequestMapping("/filetransfer")
@Tag(name = "filetransfer", description = "该接口为文件传输接口，主要用来做文件的上传、下载和预览")
public class FiletransferController {
    @Resource
    IFileTransferService fileTransferService;
    @Resource
    IFileService fileService;
    @Resource
    IUserFileService userFileService;
    @Resource
    IStorageService storageService;
    @Resource
    FileDealComp fileDealComp;
    @Resource
    UFOPFactory ufopFactory;

    public static final String CURRENT_MODULE = "文件传输接口";

    @Operation(summary = "极速上传", description = "校验文件MD5判断文件是否存在，如果存在直接上传成功并返回skipUpload=true，如果不存在返回skipUpload=false需要再次调用该接口的POST方法", tags = {"filetransfer"})
    @GetMapping(value = "/uploadfile")
    @MyLog(operation = "极速上传", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<UploadFileVo> uploadFileSpeed(UploadFileDTO uploadFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        boolean isCheckSuccess = storageService.checkStorage(sessionUser.getUserId(), uploadFileDTO.getTotalSize());
        if(!isCheckSuccess){
            return RestResult.fail().message("存储空间不足");
        }
        UploadFileVo uploadFileVo = fileTransferService.uploadFileSpeed(uploadFileDTO);
        return  RestResult.success().data(uploadFileVo);
    }

    @Operation(summary = "上传文件", description = "真正的上传文件接口", tags = {"filetransfer"})
    @PostMapping(value = "/uploadfile")
    @MyLog(operation = "上传文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<UploadFileVo> uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDto){
        JwtUser sessionUserBean = SessionUtil.getSession();
        fileTransferService.uploadFile(request, uploadFileDto, sessionUserBean.getUserId());
        UploadFileVo uploadFileVo = new UploadFileVo();
        return RestResult.success().data(uploadFileVo);
    }

    @Operation(summary = "获取存储信息", description = "获取存储信息", tags = {"filetransfer"})
    @GetMapping(value = "/getstorage")
    @MyLog(operation = "获取存储信息", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<StorageEntity> getStorage(){
        JwtUser jwtUser = SessionUtil.getSession();
        StorageEntity storage = fileTransferService.getStorage(jwtUser);
        return RestResult.success().data(storage);
    }

    @Operation(summary = "预览文件", description = "用于预览文件", tags = {"filetransfer"})
    @GetMapping(value = "/preview")
    @MyLog(operation = "预览文件", module = CURRENT_MODULE)
    @ResponseBody
    public void preview(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, PreviewDTO previewDTO) throws IOException {

        if(previewDTO.getPlatform() != null && previewDTO.getPlatform() == 2){
            fileTransferService.previewPictureFile(httpServletResponse, previewDTO);
            return ;
        }
        String token = "";
        if (StringUtils.isNotEmpty(previewDTO.getToken())){
            token = previewDTO.getToken();
        } else {
            Cookie[] cookies = httpServletRequest.getCookies();
            if(cookies != null){
                for (Cookie cookie : cookies) {
                    if("token".equals(cookie.getName())){
                        token = cookie.getValue();
                    }
                }
            }
        }
        UserFileEntity userFile = userFileService.getById(previewDTO.getUserFileId());
        boolean authResult = fileDealComp.checkAuthDownloadAndPreview(previewDTO.getShareBatchNum(),
                previewDTO.getExtractionCode(),
                token,
                previewDTO.getUserFileId(),
                previewDTO.getPlatform());
        if(!authResult) {
            log.error("没有权限预览！！！");
            return;
        }

        String fileName = userFile.getFileName() + "." + userFile.getExtendName();
        try {
            fileName = new String(fileName.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        httpServletResponse.addHeader("Content-Disposition", "fileName=" + fileName);
        String mime = MimeUtils.getMime(userFile.getExtendName());
        httpServletResponse.setHeader("Content-Type", mime);

        FileEntity fileEntity = fileService.getById(userFile.getFileId());
        if ((UFOPUtils.isVideoFile(userFile.getExtendName()) || "mp3".equalsIgnoreCase(userFile.getExtendName()) || "flac".equalsIgnoreCase(userFile.getExtendName()))
                && !"true".equals(previewDTO.getIsMin())) {
            String rangeString = httpServletRequest.getHeader("Range");
            int start = 0;
            if (StringUtils.isNotBlank(rangeString)) {
                start = Integer.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
            }

            Downloader downloader = ufopFactory.getDownloader(fileEntity.getStorageType());
            DownloadFile downloadFile = new DownloadFile();

            downloadFile.setFileUrl(fileEntity.getFileUrl());
            Range range = new Range();
            range.setStart(start);
            range.setLength(1024 * 1024 * 1);
            downloadFile.setRange(range);
            InputStream inputStream = downloader.getInputStream(downloadFile);

            OutputStream outputStream = httpServletResponse.getOutputStream();
            try {
                //返回码需要为206，代表只处理了部分请求，响应了部分数据
                httpServletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                // 每次请求只返回1MB的视频流
                httpServletResponse.setHeader("Accept-Ranges", "bytes");
                //设置此次相应返回的数据范围
                httpServletResponse.setHeader("Content-Range", "bytes " + start + "-" + (fileEntity.getFileSize() - 1) + "/" + fileEntity.getFileSize());
                IOUtils.copy(inputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
                if (downloadFile.getOssClient() != null) {
                    downloadFile.getOssClient().shutdown();
                }
            }
        } else {
            fileTransferService.previewFile(httpServletResponse, previewDTO);
        }
    }

    @Operation(summary = "下载文件", description = "下载文件", tags = {"filetransfer"})
    @GetMapping(value = "/downloadfile")
    @MyLog(operation = "预览文件", module = CURRENT_MODULE)
    @ResponseBody
    public void downloadFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, DownloadFileDTO downloadFileDTO){
        Cookie[] cookies = httpServletRequest.getCookies();
        String token = "";
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if("token".equals(cookie.getName())){
                    token = cookie.getValue();
                }
            }
        }
        boolean authResult = fileDealComp.checkAuthDownloadAndPreview(downloadFileDTO.getShareBatchNum(),
                downloadFileDTO.getExtractionCode(), token, downloadFileDTO.getUserFileId(), null);
        if (!authResult) {
            log.error("没有权限下载！！！");
            return;
        }
        //设置强制下载
        httpServletResponse.setContentType("application/force-download");
        UserFileEntity userFIle = userFileService.getById(downloadFileDTO.getUserFileId());
        String fileName = "";
        if(userFIle.getIsDir() == 1){
            fileName = userFIle.getFileName() + ".zip";
        } else {
            fileName = userFIle.getFileName() + "." + userFIle.getExtendName();
        }
        try {
            fileName = new String(fileName.getBytes("utf-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //设置文件名
        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        fileTransferService.downloadFile(httpServletResponse, downloadFileDTO);
    }

    @Operation(summary = "批量下载文件", description = "批量下载文件", tags = {"filetransfer"})
    @GetMapping(value = "/batchDownloadFile")
    @MyLog(operation = "批量下载文件", module = CURRENT_MODULE)
    @ResponseBody
    public void batchDownloadFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BatchDownloadFileDTO batchDownloadFileDTO){
        String Ids = batchDownloadFileDTO.getUserFileIds();
        String[] userFileIdStrings = Ids.split(",");
        List<String> userFileIds = new ArrayList<>();
        for (String userFileId : userFileIdStrings) {
            UserFileEntity userFile = userFileService.getById(userFileId);
            if(userFile.getIsDir() == 0){
                userFileIds.add(userFileId);
            } else {
                WhiteFile whiteFile = new WhiteFile(userFile.getFilePath(), userFile.getFileName(), true);
                List<UserFileEntity> userFileList = userFileService.selectUserFileByLikeRightFilePath(whiteFile.getPath(), userFile.getUserId());
                List<String> userFileIdsInDir = userFileList.stream().map(UserFileEntity::getUserFileId).collect(Collectors.toList());
                userFileIds.add(userFile.getUserFileId());
                userFileIds.addAll(userFileIdsInDir);
            }
        }
        UserFileEntity userFile = userFileService.getById(userFileIdStrings[0]);
        httpServletResponse.setContentType("application/force-download");
        Date date = new Date();
        String fileName = String.valueOf(date.getTime());
        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + fileName + ".zip");// 设置文件名
        fileTransferService.downloadUserFileList(httpServletResponse, userFile.getFilePath(), fileName, userFileIds);
    }

}
