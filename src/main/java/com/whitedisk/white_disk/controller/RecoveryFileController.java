package com.whitedisk.white_disk.controller;

import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.AsyncTaskComp;
import com.whitedisk.white_disk.service.api.IRecoveryFileService;
import com.whitedisk.white_disk.service.api.IUserService;
import com.whitedisk.white_disk.vo.file.RecoveryFileListVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public RestResult<RecoveryFileListVo> getRecoveryFileList(){
        JwtUser jwtUser = SessionUtil.getSession();
        List<RecoveryFileListVo> recoveryFileList = recoveryFileService.selectRecoveryFileList(jwtUser.getUserId());
        return RestResult.success().dataList(recoveryFileList, recoveryFileList.size());

    }
}
