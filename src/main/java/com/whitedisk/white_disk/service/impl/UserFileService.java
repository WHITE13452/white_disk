package com.whitedisk.white_disk.service.impl;

import cn.hutool.core.net.URLDecoder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.vo.file.FileListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
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
}
