package com.whitedisk.white_disk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.security.JwtUser;
import com.whitedisk.white_disk.entity.user.UserEntity;
import com.whitedisk.white_disk.entity.user.UserLoginInfoEntity;
import com.whitedisk.white_disk.mapper.UserLoginInfoMapper;
import com.whitedisk.white_disk.service.api.IUserLoginInfoService;
import com.whitedisk.white_disk.service.api.IUserService;
import com.whitedisk.white_disk.vo.user.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author white
 */
@Service
@Slf4j
public class UserLoginInfoImplService extends ServiceImpl<UserLoginInfoMapper, UserLoginInfoEntity> implements IUserLoginInfoService {

    @Resource
    private UserLoginInfoMapper userLoginInfoMapper;
    @Autowired
    private IUserService userService;

    @Override
    public RestResult<UserLoginVO> checkLoginUserInfo(JwtUser sessionUserEntity) {
        UserLoginVO userLoginVO=new UserLoginVO();

        if(sessionUserEntity != null && "anonymousUser".equals(sessionUserEntity.getUsername())){
            LambdaQueryWrapper<UserLoginInfoEntity> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(UserLoginInfoEntity::getUserId,sessionUserEntity.getUserId());
            wrapper.likeRight(UserLoginInfoEntity::getUserloginDate, DateUtil.getCurrentTime().substring(0,10));
            userLoginInfoMapper.delete(wrapper);
            UserLoginInfoEntity userLoginInfo=new UserLoginInfoEntity();
            userLoginInfo.setUserId(sessionUserEntity.getUserId());
            userLoginInfo.setUserloginDate(DateUtil.getCurrentTime());
            userLoginInfoMapper.insert(userLoginInfo);
            UserEntity user=userService.getById(sessionUserEntity.getUserId());
            BeanUtil.copyProperties(user,userLoginVO);
            return RestResult.success().data(userLoginVO);
        }
        return RestResult.fail().message("用户未登录");
    }
}
