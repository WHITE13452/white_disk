package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IUserFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author white
 */
@Slf4j
@Service
public class UserFileService extends ServiceImpl<UserFileMapper, UserFileEntity> implements IUserFileService {

    @Resource
    private UserFileMapper userFileMapper;

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
}
