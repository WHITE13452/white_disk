package com.whitedisk.white_disk.controller;

import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.qiwenshare.ufop.factory.UFOPFactory;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.UploadFileDTO;
import com.whitedisk.white_disk.service.api.IFileService;
import com.whitedisk.white_disk.service.api.IFileTransferService;
import com.whitedisk.white_disk.service.api.IStorageService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.vo.file.UploadFileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.PostRemove;
import javax.servlet.http.HttpServletRequest;

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
}
