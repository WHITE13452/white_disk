package com.whitedisk.white_disk.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import co.elastic.clients.elasticsearch.features.ResetFeaturesRequest;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.*;
import com.whitedisk.white_disk.entity.Share;
import com.whitedisk.white_disk.entity.ShareFile;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.service.api.IShareFileService;
import com.whitedisk.white_disk.service.api.IShareService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.vo.share.ShareFileListVO;
import com.whitedisk.white_disk.vo.share.ShareFileVO;
import com.whitedisk.white_disk.vo.share.ShareListVO;
import com.whitedisk.white_disk.vo.share.ShareTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kotlin.jvm.internal.Lambda;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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

    @Operation(summary = "保存分享文件", description = "用来将别人分享的文件保存到自己的网盘中", tags = {"share"})
    @PostMapping(value = "/savesharefile")
    @MyLog(operation = "保存分享文件", module = CURRENT_MODULE)
    @Transactional(rollbackFor=Exception.class)
    @ResponseBody
    public RestResult saveShareFile(@RequestBody SaveShareFileDTO shareFileDTO){
        JwtUser jwtUser = SessionUtil.getSession();
        List<ShareFile> shareFileList = JSON.parseArray(shareFileDTO.getFiles(), ShareFile.class);
        String saveFilePath = shareFileDTO.getFilePath();
        String userId = jwtUser.getUserId();

        List<UserFileEntity> saveUserFileList = new ArrayList<>();
        for (ShareFile shareFile : shareFileList) {
            UserFileEntity userFile = userFileService.getById(shareFile.getUserFileId());
            String fileName = userFile.getFileName();
            String saveFileName = fileDealComp.getRepeatFileName(userFile, saveFilePath);
            if (userFile.getIsDir() == 1){
                List<UserFileEntity> userFileList = userFileService.selectUserFileByLikeRightFilePath(
                        new WhiteFile(userFile.getFilePath(), userFile.getFileName(), true).getPath(),
                        userFile.getUserId());
                log.info("查询文件列表：" + JSON.toJSONString(userFileList));
                String filePath = userFile.getFilePath();
                userFileList.forEach(p->{
                    p.setUserFileId(IdUtil.getSnowflakeNextIdStr());
                    p.setUserId(userId);
                    p.setFilePath(p.getFilePath().replaceFirst(filePath + "/" + fileName, saveFilePath + "/" + saveFileName));
                    saveUserFileList.add(p);
                    log.info("当前文件：" + JSON.toJSONString(p));
                });
            }
            userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
            userFile.setUserId(userId);
            userFile.setFileName(saveFileName);
            userFile.setFilePath(saveFilePath);
        }
        log.info("----------" + JSON.toJSONString(saveUserFileList));
        userFileService.saveBatch(saveUserFileList);
        return RestResult.success();
    }

    @Operation(summary = "查看已分享列表", description = "查看已分享列表", tags = {"share"})
    @GetMapping(value = "/shareList")
    @ResponseBody
    public RestResult<ShareListVO> shareFileList(ShareListDTO shareListDTO) {
        JwtUser jwtUser = SessionUtil.getSession();
        List<ShareListVO> shareList = shareService.selectShareList(shareListDTO, jwtUser.getUserId());
        int total = shareService.selectShareListTotalCount(shareListDTO, jwtUser.getUserId());
        return RestResult.success().dataList(shareList, total);

    }

    @Operation(summary = "分享文件列表", description = "分享列表", tags = {"share"})
    @GetMapping(value = "/sharefileList")
    @ResponseBody
    public RestResult<ShareFileListVO> shareFileList(ShareFileListDTO shareFileListBySecretDTO) {
        String shareBatchNum = shareFileListBySecretDTO.getShareBatchNum();
        String shareFilePath = shareFileListBySecretDTO.getShareFilePath();
        List<ShareFileListVO> list = shareFileService.selectShareFileList(shareBatchNum, shareFilePath);
        for (ShareFileListVO shareFileListVO : list) {
            shareFileListVO.setShareFilePath(shareFilePath);
        }
        return RestResult.success().dataList(list, list.size());
    }

    @Operation(summary = "分享类型", description = "可用此接口判断是否需要提取码", tags = {"share"})
    @GetMapping(value = "/sharetype")
    @ResponseBody
    public RestResult<ShareTypeVO> shareType(ShareTypeDTO shareTypeDTO) {
        LambdaQueryWrapper<Share> shareLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shareLambdaQueryWrapper.eq(Share::getShareBatchNum, shareTypeDTO.getShareBatchNum());
        Share share = shareService.getOne(shareLambdaQueryWrapper);
        ShareTypeVO shareTypeVO = new ShareTypeVO();
        shareTypeVO.setShareType(share.getShareType());
        return RestResult.success().data(shareTypeVO);
    }

    @Operation(summary = "校验提取码", description = "校验提取码", tags = {"share"})
    @GetMapping(value = "/checkextractioncode")
    @ResponseBody
    public RestResult<String> checkExtractionCode(CheckExtractionCodeDTO checkExtractionCodeDTO) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getExtractionCode, checkExtractionCodeDTO.getExtractionCode())
                .eq(Share::getShareBatchNum, checkExtractionCodeDTO.getShareBatchNum());
        List<Share> list = shareService.list(wrapper);
        if (list.isEmpty()) {
            return RestResult.fail().message("校验失败");
        } else {
            return RestResult.success();
        }
    }

    @Operation(summary = "校验过期时间", description = "校验过期时间", tags = {"share"})
    @GetMapping(value = "/checkendtime")
    @ResponseBody
    public RestResult<String> checkEndTime(CheckEndTimeDTO checkEndTimeDTO) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getShareBatchNum, checkEndTimeDTO.getShareBatchNum());
        Share share = shareService.getOne(wrapper);
        if (share == null) {
            return RestResult.fail().message("文件不存在！");
        }
        String endTime = share.getEndTime();
        Date endTimeDate = null;
        try {
            endTimeDate = DateUtil.getDateByFormatString(endTime, "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            log.error("日期格式解析失败：{}" , e);
        }
        if (new Date().after(endTimeDate)) {
            return RestResult.fail().message("分享已过期");
        } else {
            return RestResult.success();
        }
    }

}
