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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class UserLoginInfoImplService extends ServiceImpl<UserLoginInfoMapper, UserLoginInfoEntity> implements IUserLoginInfoService {

}
