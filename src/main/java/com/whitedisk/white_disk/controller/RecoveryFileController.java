package com.whitedisk.white_disk.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.AsyncTaskComp;
import com.whitedisk.white_disk.dto.file.BatchDeleteRecoveryFileDTO;
import com.whitedisk.white_disk.dto.file.DeleteFileDTO;
import com.whitedisk.white_disk.dto.file.RestoreFileDTO;
import com.whitedisk.white_disk.entity.RecoveryFile;
import com.whitedisk.white_disk.service.api.IRecoveryFileService;
import com.whitedisk.white_disk.service.api.IUserService;
import com.whitedisk.white_disk.vo.file.RecoveryFileListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author white
 */
@Tag(name = "recoveryfile", description = "该接口主要是对回收站文件进行管理")
@Slf4j
@RestController
@RequestMapping("/recoveryfile")
public class RecoveryFileController {

    public static final String CURRENT_MODULE = "回收站文件接口";

    @Resource
    AsyncTaskComp asyncTaskComp;
    @Resource
    IRecoveryFileService recoveryFileService;


    @Operation(summary = "回收文件列表", description = "回收文件列表", tags = {"recoveryfile"})
    @GetMapping(value = "/list")
    @MyLog(operation = "回收站列表", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<RecoveryFileListVo> getRecoveryFileList(){
        JwtUser jwtUser = SessionUtil.getSession();
        List<RecoveryFileListVo> recoveryFileList = recoveryFileService.selectRecoveryFileList(jwtUser.getUserId());
        return RestResult.success().dataList(recoveryFileList, recoveryFileList.size());
    }

    @Operation(summary = "还原文件", description = "还原文件", tags = {"recoveryfile"})
    @PostMapping(value = "/restorefile")
    @MyLog(operation = "还原文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult restoreFile(@RequestBody RestoreFileDTO restoreFileDTO){
        JwtUser jwtUser = SessionUtil.getSession();
        recoveryFileService.restoreFile(restoreFileDTO.getDeleteBatchNum(), restoreFileDTO.getFilePath(), jwtUser.getUserId());
        return RestResult.success().message("还原成功");
    }

    @Operation(summary = "删除回收文件", description = "删除回收文件", tags = {"recoveryfile"})
    @MyLog(operation = "删除回收文件", module = CURRENT_MODULE)
    @PostMapping(value = "/deleterecoveryfile")
    @ResponseBody
    public RestResult<String> deleteRecoveryFile(@RequestBody DeleteFileDTO deleteFileDTO) {
        RecoveryFile recoveryFile = recoveryFileService.getOne(new QueryWrapper<RecoveryFile>().lambda().eq(RecoveryFile::getUserFileId, deleteFileDTO.getUserFileId()));
        asyncTaskComp.deleteUserFile(recoveryFile.getUserFileId());
        recoveryFileService.removeById(recoveryFile.getRecoveryFileId());
        return RestResult.success().data("删除成功");
    }

    @Operation(summary = "批量删除回收文件", description = "批量删除回收文件", tags = {"recoveryfile"})
    @RequestMapping(value = "/batchdelete", method = RequestMethod.POST)
    @MyLog(operation = "批量删除回收文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> batchDeleteRecoveryFile(@RequestBody BatchDeleteRecoveryFileDTO batchDeleteRecoveryFileDTO) {
        String userFileIds = batchDeleteRecoveryFileDTO.getUserFileIds();
        String[] userFileIdList = userFileIds.split(",");
        for (String userFileId : userFileIdList) {
            RecoveryFile recoveryFile = recoveryFileService.getOne(new QueryWrapper<RecoveryFile>().lambda().eq(RecoveryFile::getUserFileId, userFileId));
            if (recoveryFile != null) {
                asyncTaskComp.deleteUserFile(recoveryFile.getUserFileId());
                recoveryFileService.removeById(recoveryFile.getRecoveryFileId());
            }
        }
        return RestResult.success().data("批量删除成功");
    }
}
