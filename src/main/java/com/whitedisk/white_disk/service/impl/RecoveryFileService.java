package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.entity.RecoveryFile;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.RecoveryFileMapper;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IRecoveryFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.vo.file.RecoveryFileListVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class RecoveryFileService extends ServiceImpl<RecoveryFileMapper,RecoveryFile> implements IRecoveryFileService {
    @Resource
    RecoveryFileMapper recoveryFileMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    FileDealComp fileDealComp;

    @Override
    public List<RecoveryFileListVo> selectRecoveryFileList(String userId) {
        return recoveryFileMapper.selectRecoveryFileList(userId);
    }

    @Override
    public void restoreFile(String deleteBatchNum, String filePath, String sessionUserId) {
        List<UserFileEntity> restoreUserFileList = userFileMapper.selectList(new QueryWrapper<UserFileEntity>().lambda().eq(UserFileEntity::getDeleteBatchNum, deleteBatchNum));
        for (UserFileEntity restoreUserFile : restoreUserFileList) {
            restoreUserFile.setDeleteFlag(0);
            restoreUserFile.setDeleteBatchNum(deleteBatchNum);
            String fileName = fileDealComp.getRepeatFileName(restoreUserFile, restoreUserFile.getFilePath());
            if (restoreUserFile.isDirectory()) {
                if (!StringUtils.equals(fileName, restoreUserFile.getFileName())) {
                    userFileMapper.deleteById(restoreUserFile);
                } else {
                    userFileMapper.updateById(restoreUserFile);
                }
            } else if (restoreUserFile.isFile()) {
                restoreUserFile.setFileName(fileName);
                userFileMapper.updateById(restoreUserFile);
            }
        }


        WhiteFile whiteFile = new WhiteFile(filePath, true);
        fileDealComp.restoreParentFilePath(whiteFile, sessionUserId);

        fileDealComp.deleteRepeatSubDirFile(filePath, sessionUserId);

        LambdaQueryWrapper<RecoveryFile> recoveryFileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        recoveryFileLambdaQueryWrapper.eq(RecoveryFile::getDeleteBatchNum, deleteBatchNum);
        recoveryFileMapper.delete(recoveryFileLambdaQueryWrapper);
    }

    @Override
    public void deleteUserFileByDeleteBatchNum(String deleteBatchNum) {
        LambdaQueryWrapper<UserFileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFileEntity::getDeleteBatchNum, deleteBatchNum);
        userFileMapper.deleteById(wrapper);
    }
}
