package com.whitedisk.white_disk.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.MimeUtils;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.qiwenshare.ufop.constant.StorageTypeEnum;
import com.qiwenshare.ufop.constant.UploadFileStatusEnum;
import com.qiwenshare.ufop.exception.operation.DownloadException;
import com.qiwenshare.ufop.exception.operation.UploadException;
import com.qiwenshare.ufop.factory.UFOPFactory;
import com.qiwenshare.ufop.operation.delete.Deleter;
import com.qiwenshare.ufop.operation.delete.domain.DeleteFile;
import com.qiwenshare.ufop.operation.download.Downloader;
import com.qiwenshare.ufop.operation.download.domain.DownloadFile;
import com.qiwenshare.ufop.operation.preview.Previewer;
import com.qiwenshare.ufop.operation.preview.domain.PreviewFile;
import com.qiwenshare.ufop.operation.upload.Uploader;
import com.qiwenshare.ufop.operation.upload.domain.UploadFile;
import com.qiwenshare.ufop.operation.upload.domain.UploadFileResult;
import com.qiwenshare.ufop.util.UFOPUtils;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.DownloadFileDTO;
import com.whitedisk.white_disk.dto.file.PreviewDTO;
import com.whitedisk.white_disk.dto.file.UploadFileDTO;
import com.whitedisk.white_disk.entity.*;
import com.whitedisk.white_disk.mapper.*;
import com.whitedisk.white_disk.service.api.IFileTransferService;
import com.whitedisk.white_disk.service.api.IStorageService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.utils.WhiteFileUtil;
import com.whitedisk.white_disk.vo.file.UploadFileVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class FileTransferService implements IFileTransferService {

    @Resource
    FileMapper fileMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    UploadTaskDetailMapper uploadTaskDetailMapper;
    @Resource
    UploadTaskMapper uploadTaskMapper;
    @Resource
    UFOPFactory ufopFactory;
    @Resource
    FileDealComp fileDealComp;
    @Resource
    IStorageService storageService;
    @Resource
    PictureFileMapper pictureFileMapper;

    @Resource
    ImageMapper imageMapper;

    @Override
    public UploadFileVo uploadFileSpeed(UploadFileDTO uploadFileDTO) {
        UploadFileVo uploadFileVo = new UploadFileVo();
        Map<String, Object> param = new HashMap<>();
        param.put("identifier", uploadFileDTO.getIdentifier());
        List<FileEntity> list = fileMapper.selectByMap(param);

        String filePath = uploadFileDTO.getFilePath();
        String relativePath = uploadFileDTO.getRelativePath();
        WhiteFile whiteFile = null;
        if (relativePath.contains("/")){
            whiteFile = new WhiteFile(filePath,relativePath,false);
        }else{
            whiteFile = new WhiteFile(filePath, uploadFileDTO.getFilename(),false);
        }

        if (list != null && !list.isEmpty()) {
            FileEntity fileEntity = list.get(0);
            UserFileEntity userFile = new UserFileEntity(whiteFile, SessionUtil.getUserId(), fileEntity.getFileId());
            UserFileEntity searchParam = WhiteFileUtil.searchWhiteFileParam(userFile);
            //查看是否存在该文件切片
            List<UserFileEntity> userFileEntityList = userFileMapper.selectList(new QueryWrapper<>(searchParam));
            //若没有，就是第一片，插入
            if(userFileEntityList.size() <= 0){
                userFileMapper.insert(userFile);
                fileDealComp.uploadESByUserFileId(userFile.getUserFileId());
            }
            if (relativePath.contains("/")){
                fileDealComp.restoreParentFilePath(whiteFile, SessionUtil.getUserId());
            }

            uploadFileVo.setSkipUpload(true);
        }else{
            uploadFileVo.setSkipUpload(false);

            List<Integer> uploaded = uploadTaskDetailMapper.selectUploadedChunkNumList(uploadFileDTO.getIdentifier());
            if (uploaded != null && !uploaded.isEmpty()) {
                uploadFileVo.setUploaded(uploaded);
            }else{
                LambdaQueryWrapper<UploadTask> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UploadTask::getIdentifier, uploadFileDTO.getIdentifier());
                List<UploadTask> rsList = uploadTaskMapper.selectList(wrapper);
                if (rsList == null || rsList.isEmpty()){
                    UploadTask uploadTask = new UploadTask();
                    uploadTask.setIdentifier(uploadFileDTO.getIdentifier());
                    uploadTask.setUploadTime(DateUtil.getCurrentTime());
                    uploadTask.setUploadStatus(UploadFileStatusEnum.UNCOMPLATE.getCode());
                    uploadTask.setFileName(whiteFile.getNameNotExtend());
                    uploadTask.setFilePath(whiteFile.getParent());
                    uploadTask.setExtendName(whiteFile.getExtendName());
                    uploadTask.setUserId(SessionUtil.getUserId());
                    uploadTaskMapper.insert(uploadTask);
                }
            }
        }
        return uploadFileVo;
    }

    @Override
    public void uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO, String userId) {
        UploadFile uploadFile = new UploadFile();

        uploadFile.setIdentifier(uploadFileDTO.getIdentifier());
        uploadFile.setChunkNumber(uploadFileDTO.getChunkNumber());
        uploadFile.setChunkSize(uploadFileDTO.getChunkSize());
        uploadFile.setTotalChunks(uploadFileDTO.getTotalChunks());
        uploadFile.setTotalSize(uploadFileDTO.getTotalSize());
        uploadFile.setCurrentChunkSize(uploadFileDTO.getCurrentChunkSize());

        Uploader uploader = ufopFactory.getUploader();
        if (uploader == null) {
            log.error("上传失败，请检查storageType是否配置正确");
            throw new UploadException("上传失败");
        }
        List<UploadFileResult> uploadFileResultList;
        try{
            uploadFileResultList = uploader.upload(request, uploadFile);
        }catch (Exception e){
            log.error("上传失败，请检查UFOP连接配置是否正确");
            throw new UploadException("上传失败", e);
        }

        for (int i = 0; i < uploadFileResultList.size(); i++) {
            UploadFileResult uploadFileResult = uploadFileResultList.get(i);
            String relativePath = uploadFileDTO.getRelativePath();
            WhiteFile whiteFile=null;
            if(relativePath.contains("/")){
                whiteFile = new WhiteFile(uploadFileDTO.getFilePath(), relativePath, false);
            } else {
                whiteFile = new WhiteFile(uploadFileDTO.getFilePath(), uploadFileDTO.getFilename(), false);
            }

            if(UploadFileStatusEnum.SUCCESS.equals(uploadFileResult.getStatus())){
                FileEntity fileEntity = new FileEntity(uploadFileResult);
                fileEntity.setCreateUserId(userId);
                fileMapper.insert(fileEntity);

                UserFileEntity userFile = new UserFileEntity(whiteFile, userId, fileEntity.getFileId());
                UserFileEntity param = WhiteFileUtil.searchWhiteFileParam(userFile);
                List<UserFileEntity> userFileEntityList = userFileMapper.selectList(new QueryWrapper<>(param));

                if (userFileEntityList.size() > 0){
                    String fileName = fileDealComp.getRepeatFileName(userFile, userFile.getFilePath());
                    userFile.setFileName(fileName);
                }
                userFileMapper.insert(userFile);

                if(relativePath.contains("/")){
                    fileDealComp.restoreParentFilePath(whiteFile ,userId);
                }

                fileDealComp.uploadESByUserFileId(userFile.getUserFileId());

                LambdaQueryWrapper<UploadTaskDetail> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UploadTaskDetail::getIdentifier, uploadFileDTO.getIdentifier());
                uploadTaskDetailMapper.delete(queryWrapper);

                LambdaUpdateWrapper<UploadTask> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(UploadTask::getUploadStatus,UploadFileStatusEnum.SUCCESS.getCode())
                        .eq(UploadTask::getIdentifier, uploadFileDTO.getIdentifier());
                uploadTaskMapper.update(null, updateWrapper);

                try{
                    if(UFOPUtils.isImageFile(uploadFileResult.getExtendName())){
                        BufferedImage src = uploadFileResult.getBufferedImage();
                        Image image = new Image();
                        image.setFileId(fileEntity.getFileId());
                        image.setImageWidth(src.getWidth());
                        image.setImageHeight(src.getHeight());
                        imageMapper.insert(image);
                    }
                } catch (Exception e) {
                    log.error("生成图片缩略图失败！", e);
                }

                fileDealComp.parseMusicFile(uploadFileResult.getExtendName(), uploadFileResult.getStorageType().getCode(), uploadFileResult.getFileUrl(), fileEntity.getFileId());
            } else if (UploadFileStatusEnum.UNCOMPLATE.equals(uploadFileResult.getStatus())){
                UploadTaskDetail uploadTaskDetail = new UploadTaskDetail();
                uploadTaskDetail.setFilePath(whiteFile.getParent());
                uploadTaskDetail.setFilename(whiteFile.getNameNotExtend());
                uploadTaskDetail.setChunkNumber(uploadFileDTO.getChunkNumber());
                uploadTaskDetail.setChunkSize((int)uploadFileDTO.getChunkSize());
                uploadTaskDetail.setRelativePath(uploadFileDTO.getRelativePath());
                uploadTaskDetail.setTotalChunks(uploadFileDTO.getTotalChunks());
                uploadTaskDetail.setTotalSize((int)uploadFileDTO.getTotalSize());
                uploadTaskDetail.setIdentifier(uploadFileDTO.getIdentifier());
                uploadTaskDetailMapper.insert(uploadTaskDetail);
            } else if (UploadFileStatusEnum.FAIL.equals(uploadFileResult.getStatus())){
                LambdaQueryWrapper<UploadTaskDetail> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UploadTaskDetail::getIdentifier, uploadFileDTO.getIdentifier());
                uploadTaskDetailMapper.delete(queryWrapper);

                LambdaUpdateWrapper<UploadTask> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.set(UploadTask::getUploadStatus, UploadFileStatusEnum.FAIL.getCode())
                        .eq(UploadTask::getIdentifier, uploadFileDTO.getIdentifier());
                uploadTaskMapper.update(null, lambdaUpdateWrapper);
            }
        }
    }

    @Override
    public StorageEntity getStorage(JwtUser user) {
//        StorageEntity storageEntity = new StorageEntity();
//        storageEntity.setUserId(user.getUserId());
        Long storageSize = userFileMapper.selectStorageSizeByUserId(user.getUserId());
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setStorageSize(storageSize);
        Long totalStorageSize = storageService.getTotalStorageSize(user.getUserId());
        storageEntity.setTotalStorageSize(totalStorageSize);
        storageEntity.setUserId(user.getUserId());
        return storageEntity;
    }

    @Override
    public void previewPictureFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO) {
        byte[] bytesUrl = Base64.getDecoder().decode(previewDTO.getUrl());
        PictureFile pictureFile = new PictureFile();
        pictureFile.setFileUrl(new String(bytesUrl));

        pictureFile = pictureFileMapper.selectOne(new QueryWrapper<>(pictureFile));
        Previewer previewer = ufopFactory.getPreviewer(pictureFile.getStorageType());
        if(previewer == null){
            log.error("预览失败，文件存储类型不支持预览，storageType:{}", pictureFile.getStorageType());
            throw new UploadException("预览失败");
        }

        PreviewFile previewFile = new PreviewFile();
        previewFile.setFileUrl(pictureFile.getFileUrl());
        try{
            String mime= MimeUtils.getMime(pictureFile.getExtendName());
            httpServletResponse.setHeader("Content-Type", mime);

            String fileName = pictureFile.getFileName() + "." + pictureFile.getExtendName();
            try {
                fileName = new String(fileName.getBytes("utf-8"), "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            httpServletResponse.addHeader("Content-Disposition", "fileName=" + fileName);// 设置文件名

            previewer.imageOriginalPreview(httpServletResponse, previewFile);
        } catch (Exception e){
            //org.apache.catalina.connector.ClientAbortException: java.io.IOException: 你的主机中的软件中止了一个已建立的连接。
            if (e.getMessage().contains("ClientAbortException")) {
                //该异常忽略不做处理
            } else {
                log.error("预览文件出现异常：{}", e.getMessage());
            }

        }
    }

    @Override
    public void previewFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO) {
        UserFileEntity userFile = userFileMapper.selectById(previewDTO.getUserFileId());
        FileEntity fileEntity = fileMapper.selectById(userFile.getFileId());
        Previewer previewer = ufopFactory.getPreviewer(fileEntity.getStorageType());
        if(previewer == null) {
            log.error("预览失败，文件存储类型不支持预览，storageType:{}", fileEntity.getStorageType());
            throw new UploadException("预览失败");
        }
        PreviewFile previewFile = new PreviewFile();
        previewFile.setFileUrl(fileEntity.getFileUrl());

        try {
            //是否产生缩略图
            if ("true".equals(previewDTO.getIsMin())) {
                previewer.imageThumbnailPreview(httpServletResponse, previewFile);
            } else {
                previewer.imageOriginalPreview(httpServletResponse, previewFile);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("ClientAbortException")) {
                //该异常忽略不做处理
            } else {
                log.error("预览文件出现异常：{}", e.getMessage());
            }
        }
    }

    @Override
    public void downloadFile(HttpServletResponse httpServletResponse, DownloadFileDTO downloadFileDTO) {
        UserFileEntity userFile = userFileMapper.selectById(downloadFileDTO.getUserFileId());
        if(userFile.isFile()){
            FileEntity fileEntity = fileMapper.selectById(userFile.getFileId());
            Downloader downloader = ufopFactory.getDownloader(fileEntity.getStorageType());
            if (downloader == null) {
                log.error("下载失败，文件存储类型不支持下载，storageType:{}", fileEntity.getStorageType());
                throw new DownloadException("下载失败");
            }
            DownloadFile downloadFile = new DownloadFile();

            downloadFile.setFileUrl(fileEntity.getFileUrl());
            httpServletResponse.setContentLengthLong(fileEntity.getFileSize());
            downloader.download(httpServletResponse, downloadFile);
        } else {
            WhiteFile whiteFile = new WhiteFile(userFile.getFilePath(), userFile.getFileName(), true);
            List<UserFileEntity> userFileList = userFileMapper.selectUserFileByLikeRightFilePath(whiteFile.getPath(), userFile.getUserId());
            List<String> userFileIds = userFileList.stream().map(UserFileEntity::getUserFileId).collect(Collectors.toList());

            downloadUserFileList(httpServletResponse, userFile.getFilePath(), userFile.getFileName(), userFileIds);
        }
    }

    @Override
    public void downloadUserFileList(HttpServletResponse httpServletResponse, String filePath, String fileName, List<String> userFileIds) {
        String staticPath = UFOPUtils.getStaticPath();
        String tempPath = staticPath + "temp" + File.separator;
        File tempDirFile = new File(tempPath);
        if(!tempDirFile.exists()){
            tempDirFile.mkdirs();
        }

        FileOutputStream f = null;
        try {
            f= new FileOutputStream(tempPath + fileName + ".zip");
        } catch (FileNotFoundException e) {


        }
        CheckedOutputStream checkedOutputStream = new CheckedOutputStream(f, new Adler32());
        ZipOutputStream zipOutputStream = new ZipOutputStream(checkedOutputStream);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(zipOutputStream);

        try {
            for (String userFileId : userFileIds) {
                UserFileEntity userFile = userFileMapper.selectById(userFileId);
                if(userFile.isFile()){
                    FileEntity fileEntity = fileMapper.selectById(userFile.getFileId());
                    Downloader downloader = ufopFactory.getDownloader(fileEntity.getStorageType());
                    if (downloader == null) {
                        log.error("下载失败，文件存储类型不支持下载，storageType:{}", fileEntity.getStorageType());
                        throw new UploadException("下载失败");
                    }
                    DownloadFile downloadFile = new DownloadFile();
                    downloadFile.setFileUrl(fileEntity.getFileUrl());
                    InputStream inputStream = downloader.getInputStream(downloadFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    try {
                        WhiteFile whiteFile = new WhiteFile(StrUtil.removePrefix(userFile.getFilePath(), filePath), userFile.getFileName() + "." + userFile.getExtendName(), false);
                        zipOutputStream.putNextEntry(new ZipEntry(whiteFile.getPath()));

                        byte[] buffer = new byte[1024];
                        int i = bufferedInputStream.read(buffer);
                        while (i != -1) {
                            bufferedOutputStream.write(buffer, 0, i);
                            i = bufferedInputStream.read(buffer);
                        }
                    } catch (IOException e) {
                        log.error("" + e);
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(bufferedInputStream);
                        try {
                            bufferedOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    WhiteFile whiteFile = new WhiteFile(StrUtil.removePrefix(userFile.getFilePath(), filePath), userFile.getFileName(), true);
                    //空文件夹
                    zipOutputStream.putNextEntry(new ZipEntry(whiteFile.getPath() + WhiteFile.separator));
                    zipOutputStream.closeEntry();
                }
            }
        } catch (IOException e) {
            log.error("压缩过程中出现异常:"+ e);
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String zipPath = "";
        try {
            Downloader downloader = ufopFactory.getDownloader(StorageTypeEnum.LOCAL.getCode());
            DownloadFile downloadFile = new DownloadFile();
            downloadFile.setFileUrl("temp" + File.separator + fileName + ".zip");
            File tempFile = new File(UFOPUtils.getStaticPath() + downloadFile.getFileUrl());
            httpServletResponse.setContentLengthLong(tempFile.length());
            downloader.download(httpServletResponse, downloadFile);
            zipPath = UFOPUtils.getStaticPath() + "temp" + File.separator + fileName + ".zip";
        } catch (Exception e) {
            if (e.getMessage().contains("ClientAbortException")) {
                //该异常忽略不做处理
            } else {
                log.error("下传zip文件出现异常：{}", e.getMessage());
            }
        } finally {
            File file = new File(zipPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public void deleteFile(FileEntity fileEntity) {
        Deleter deleter = null;
        deleter = ufopFactory.getDeleter(fileEntity.getStorageType());
        DeleteFile deleteFile = new DeleteFile();
        deleteFile.setFileUrl(fileEntity.getFileUrl());
        deleter.delete(deleteFile);
    }


}
