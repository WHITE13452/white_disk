package com.whitedisk.white_disk.service.impl;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.BatchDeleteFileDTO;
import com.whitedisk.white_disk.entity.RecoveryFile;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.RecoveryFileMapper;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.vo.file.FileListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.functors.ExceptionPredicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author white
 */
@Slf4j
@Service
@Transactional(rollbackFor=Exception.class)
public class UserFileService extends ServiceImpl<UserFileMapper, UserFileEntity> implements IUserFileService {

    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private RecoveryFileMapper recoveryFileMapper;
    @Resource
    private FileDealComp fileDealComp;

    private static Executor executor= Executors.newFixedThreadPool(20);

    @Override
    public List<UserFileEntity> selectUserFileByNameAndPath(String fileName, String filePath, String userId) {
        LambdaQueryWrapper<UserFileEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserFileEntity::getFileName,fileName)
                .eq(UserFileEntity::getFilePath,filePath)
                .eq(UserFileEntity::getUserId,userId)
                .eq(UserFileEntity::getDeleteFlag,0);
        return userFileMapper.selectList(wrapper);
    }

    @Override
    public List<UserFileEntity> selectUserFileByLikeRightFilePath(String filePath, String userId) {
        return userFileMapper.selectUserFileByLikeRightFilePath(filePath, userId);
    }

    @Override
    public List<UserFileEntity> selectSameUserFile(String fileName, String filePath, String extendName, String userId) {
        LambdaQueryWrapper<UserFileEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserFileEntity::getFileName,fileName)
                .eq(UserFileEntity::getFilePath,filePath)
                .eq(UserFileEntity::getExtendName,extendName)
                .eq(UserFileEntity::getUserId,userId)
                .eq(UserFileEntity::getDeleteFlag,0);
        return userFileMapper.selectList(wrapper);
    }

    @Override
    public IPage<FileListVO> userFileList(String userId, String filePath, Long currentPage, Long pageCount) {
        Page<FileListVO> page = new Page<>(currentPage,pageCount);
        UserFileEntity userFile = new UserFileEntity();
        JwtUser user = SessionUtil.getSession();
        if (userId == null) {
            userFile.setUserId(user.getUserId());
        } else {
            userFile.setUserId(userId);
        }
        userFile.setFilePath(URLDecoder.decodeForPath(filePath, StandardCharsets.UTF_8));

        return userFileMapper.selectPageVo(page,userFile,null);
    }

    @Override
    public IPage<FileListVO> getFileByFileType(Integer fileTypeId, Long currentPage, Long pageCount, String userId) {
        Page<FileListVO> page = new Page<>(currentPage, pageCount);
        UserFileEntity userFile = new UserFileEntity();
        userFile.setUserId(userId);
        return userFileMapper.selectPageVo(page,userFile,fileTypeId);
    }

    @Override
    public void deleteUserFile(String userFileId, JwtUser user) {
        UserFileEntity userFileEntity = userFileMapper.selectById(userFileId);
        String uuid = UUID.randomUUID().toString();
        if(userFileEntity.getIsDir() == 1){
            LambdaUpdateWrapper<UserFileEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(UserFileEntity::getDeleteFlag, RandomUtil.randomInt(1,999999))
                    .set(UserFileEntity::getDeleteBatchNum,uuid)
                    .set(UserFileEntity::getDeleteTime, DateUtil.getCurrentTime())
                    .eq(UserFileEntity::getUserFileId,userFileId);
            userFileMapper.update(null ,wrapper);

            String filePath = new WhiteFile(userFileEntity.getFilePath(), userFileEntity.getFileName(), true).getPath();
            updateFileDeleteStateByFilePath(filePath,uuid,user.getUserId());
        } else {
//            UserFileEntity userFileEntity1 = userFileMapper.selectById(userFileId);
            LambdaUpdateWrapper<UserFileEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(UserFileEntity::getDeleteFlag, RandomUtil.randomInt(1,999999))
                    .set(UserFileEntity::getDeleteBatchNum,uuid)
                    .set(UserFileEntity::getDeleteTime, DateUtil.getCurrentTime())
                    .eq(UserFileEntity::getUserFileId,userFileId);
            userFileMapper.update(null ,wrapper);
        }

        RecoveryFile recoveryFile = new RecoveryFile();
        recoveryFile.setUserFileId(userFileId);
        recoveryFile.setDeleteTime(DateUtil.getCurrentTime());
        recoveryFile.setDeleteBatchNum(uuid);
        recoveryFileMapper.insert(recoveryFile);

    }

    @Override
    public void userFileCopy(String userFileId, String newfilePath, String userId) {
        UserFileEntity userFile = userFileMapper.selectById(userFileId);
        String oldFilePath = userFile.getFilePath();
        String fileName = userFile.getFileName();

        userFile.setFilePath(newfilePath);
        userFile.setFileName(fileName);
        if(userFile.getIsDir() == 0){
            String repeatFileName = fileDealComp.getRepeatFileName(userFile, userFile.getFilePath());
            userFile.setFileName(repeatFileName);
        }
        try{
            userFileMapper.insert(userFile);
        }catch (Exception e){
            log.warn(e.getMessage());
        }

        oldFilePath = new WhiteFile(oldFilePath,fileName,true).getPath();
        newfilePath = new WhiteFile(newfilePath,fileName,true).getPath();

        //若是目录，移动子文件
        if(userFile.isDirectory()){
            List<UserFileEntity> subUserFileList = userFileMapper.selectUserFileByLikeRightFilePath(oldFilePath, userId);

            for (UserFileEntity newUserFile : subUserFileList) {
                newUserFile.setFilePath(newUserFile.getFilePath().replaceFirst(oldFilePath, newfilePath));
                newUserFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
                if(newUserFile.isDirectory()){
                    String repeatFileName = fileDealComp.getRepeatFileName(newUserFile, newUserFile.getFilePath());
                    newUserFile.setFileName(repeatFileName);
                }
                try {
                    userFileMapper.insert(newUserFile);
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        }
    }

    @Override
    public void updateFilepathByUserFileId(String userFileId, String newfilePath, String userId) {
        UserFileEntity userFile = userFileMapper.selectById(userFileId);
        String oldfilePath = userFile.getFilePath();
        String fileName = userFile.getFileName();

        userFile.setFilePath(newfilePath);
        if (userFile.getIsDir() == 0) {
            String repeatFileName = fileDealComp.getRepeatFileName(userFile, userFile.getFilePath());
            userFile.setFileName(repeatFileName);
        }

        try {
            userFileMapper.updateById(userFile);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        oldfilePath = new WhiteFile(oldfilePath, fileName, true).getPath();
        newfilePath = new WhiteFile(newfilePath, fileName, true).getPath();

        //如果是文件夹，移动文件夹内的文件
        if (userFile.isDirectory()) {
            List<UserFileEntity> list = selectUserFileByLikeRightFilePath(oldfilePath, userId);
            for (UserFileEntity newUserFile : list) {
                newUserFile.setFilePath(newUserFile.getFilePath().replaceFirst(oldfilePath, newfilePath));
                if (newUserFile.getIsDir() == 0){
                    String repeatFileName = fileDealComp.getRepeatFileName(newUserFile, newUserFile.getFilePath());
                    newUserFile.setFileName(repeatFileName);
                }
                try {
                    userFileMapper.updateById(newUserFile);
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        }

    }


    private void updateFileDeleteStateByFilePath(String filePath,String deleteBatchNum,String userId){
        executor.execute(()->{
            List<UserFileEntity> userFileEntities = userFileMapper.selectUserFileByLikeRightFilePath(filePath, userId);
            for (int i = 0; i < userFileEntities.size(); i++) {
                UserFileEntity userFileTemp = userFileEntities.get(i);
                LambdaUpdateWrapper<UserFileEntity> wrapper=new LambdaUpdateWrapper<>();
                wrapper.set(UserFileEntity::getDeleteFlag, RandomUtil.randomInt(1,999999))
                        .set(UserFileEntity::getDeleteBatchNum,deleteBatchNum)
                        .set(UserFileEntity::getDeleteTime, DateUtil.getCurrentTime())
                        .eq(UserFileEntity::getUserFileId,userFileTemp.getUserFileId())
                        .eq(UserFileEntity::getDeleteFlag,0);
                userFileMapper.update(null,wrapper);
            }
        });
    }
}
