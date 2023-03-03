package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.security.SessionUtil;
import com.qiwenshare.ufop.constant.UploadFileStatusEnum;
import com.qiwenshare.ufop.exception.operation.UploadException;
import com.qiwenshare.ufop.factory.UFOPFactory;
import com.qiwenshare.ufop.operation.upload.Uploader;
import com.qiwenshare.ufop.operation.upload.domain.UploadFile;
import com.qiwenshare.ufop.operation.upload.domain.UploadFileResult;
import com.qiwenshare.ufop.util.UFOPUtils;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.UploadFileDTO;
import com.whitedisk.white_disk.entity.*;
import com.whitedisk.white_disk.mapper.*;
import com.whitedisk.white_disk.service.api.IFileTransferService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.utils.WhiteFileUtil;
import com.whitedisk.white_disk.vo.file.UploadFileVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
