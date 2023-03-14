package com.whitedisk.white_disk.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.ShareFileDTO;
import com.whitedisk.white_disk.entity.Share;
import com.whitedisk.white_disk.entity.ShareFile;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.service.api.IShareFileService;
import com.whitedisk.white_disk.service.api.IShareService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.vo.file.ShareFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author white
 */
@Tag(name = "share", description = "文件分享接口")
@RestController
@Slf4j
@RequestMapping("/share")
public class ShareController {

    public static final String CURRENT_MODULE = "文件分享";

    @Resource
    IShareFileService shareFileService;
    @Resource
    IShareService shareService;
    @Resource
    IUserFileService userFileService;
    @Resource
    FileDealComp fileDealComp;

    @Operation(summary = "分享文件", description = "分享文件接口", tags = {"share"})
    @PostMapping(value = "/sharefile")
    @MyLog(operation = "分享文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<ShareFileVO> shareFile(@RequestBody ShareFileDTO shareFileDTO) {
        JwtUser jwtUser = SessionUtil.getSession();

        ShareFileVO shareFileVO = new ShareFileVO();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        Share share = new Share();
        BeanUtils.copyProperties(shareFileDTO, share);
        share.setUserId(jwtUser.getUserId());
        share.setShareTime(DateUtil.getCurrentTime());
        share.setShareStatus(0);
        if (shareFileDTO.getShareType() == 1){
            String extractionCode = RandomUtil.randomNumbers(6);
            share.setExtractionCode(extractionCode);
            shareFileVO.setExtractionCode(share.getExtractionCode());
        }
        share.setShareBatchNum(uuid);
        shareService.save(share);

        List<ShareFile> fileList = JSON.parseArray(shareFileDTO.getFiles(), ShareFile.class);
        List<ShareFile> saveFileList = new ArrayList<>();
        for (ShareFile shareFile : fileList) {
            UserFileEntity userFile = userFileService.getById(shareFile.getUserFileId());
            if (userFile.getUserId().compareTo(jwtUser.getUserId()) != 0) {
                return RestResult.fail().message("分享自己的文件！！！");
            }
            if (userFile.getIsDir() == 1){
                WhiteFile whiteFile = new WhiteFile(userFile.getFilePath(), userFile.getFileName(), true);
                List<UserFileEntity> userFileList = userFileService.selectUserFileByLikeRightFilePath(whiteFile.getPath(), jwtUser.getUserId());
                for (UserFileEntity userFileEntity : userFileList) {
                    ShareFile shareFile1 = new ShareFile();
                    shareFile1.setUserFileId(userFileEntity.getUserFileId());
                    shareFile1.setShareBatchNum(uuid);
                    shareFile1.setShareFilePath(userFileEntity.getFilePath().replaceFirst(userFile.getFilePath().equals("/") ? "" : userFile.getFilePath(), ""));
                    saveFileList.add(shareFile1);
                }
            }
            shareFile.setShareFilePath("/");
            shareFile.setShareBatchNum(uuid);
            saveFileList.add(shareFile);
        }
        shareFileService.batchInsertShareFile(saveFileList);
        shareFileVO.setShareBatchNum(uuid);
        return RestResult.success().data(shareFileVO);
    }
}