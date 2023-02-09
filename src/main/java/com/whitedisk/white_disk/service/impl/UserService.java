package com.whitedisk.white_disk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.HashUtils;
import com.qiwenshare.common.util.PasswordUtil;
import com.qiwenshare.common.util.security.JwtUser;
import com.whitedisk.white_disk.component.JwtComp;
import com.whitedisk.white_disk.component.UserDealComp;
import com.whitedisk.white_disk.dto.user.RegisterDTO;
import com.whitedisk.white_disk.entity.user.Role;
import com.whitedisk.white_disk.entity.user.UserEntity;
import com.whitedisk.white_disk.mapper.UserMapper;
import com.whitedisk.white_disk.service.api.IUserService;
import com.whitedisk.white_disk.vo.user.UserLoginVO;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author white
 */
@Slf4j
@Service
@Transactional(rollbackFor=Exception.class)
public class UserService extends ServiceImpl<UserMapper,UserEntity> implements IUserService , UserDetailsService {

    @Resource
    private UserMapper userMapper;
    @Autowired
    private UserDealComp userDealComp;
    @Resource
    private JwtComp jwtComp;


    public static Map<String, String> verificationCodeMap = new HashMap<>();

    @Override
    public String getUserIdByToken(String token) {
        Claims claims=null;
        if(StringUtils.isEmpty(token)){
            return null;
        }
        token = token.replace("Bearer ", "");
        token = token.replace("Bearer%20", "");
        try{
            claims=jwtComp.parseJWT(token);
        } catch (Exception e){
            log.error("解码异常");
            return null;
        }
        if(claims == null){
            log.error("解码异常，为空");
            return null;
        }
        String subject = claims.getSubject();;
        UserEntity user = JSON.parseObject(subject,UserEntity.class);
        UserEntity userEntity = userMapper.selectById(user.getUserId());
        if(userEntity != null){
            return userEntity.getUserId();
        }
        return null;
    }

    @Override
    public RestResult<String> registerUser(RegisterDTO registerDTO) {
        UserEntity userEntity = new UserEntity();
        BeanUtil.copyProperties(registerDTO,userEntity);

        String telephone = userEntity.getTelephone();
        verificationCodeMap.remove(telephone);

        if (userDealComp.isUserNameExit(userEntity)) {
            return RestResult.fail().message("用户名已存在！");
        }
        if (!userDealComp.isPhoneFormatRight(userEntity.getTelephone())){
            return RestResult.fail().message("手机号格式不正确！");
        }
        if (userDealComp.isPhoneExit(userEntity)) {
            return RestResult.fail().message("手机号已存在！");
        }

        String saltValue = PasswordUtil.getSaltValue();
        String newPassword = HashUtils.hashHex("MD5", userEntity.getPassword(), saltValue, 1024);

        userEntity.setSalt(saltValue);
        userEntity.setPassword(newPassword);
        userEntity.setRegisterTime(DateUtil.getCurrentTime());
        userEntity.setAvailable(1);

        int result = userMapper.insert(userEntity);
        userMapper.insertUserRole(userEntity.getUserId(),2);
        if (result == 1) {
            return RestResult.success();
        } else {
            return RestResult.fail().message("注册用户失败，请检查输入信息！");
        }
    }

    @Override
    public RestResult<UserLoginVO> userLogin(String telephone, String password) {
        RestResult<UserLoginVO> loginVo=new RestResult<UserLoginVO>();
        String salt = this.getSaltByTelephone(telephone);
        String hashPassword = HashUtils.hashHex("MD5", password, salt, 1024);
        UserEntity userEntity=this.selectUserByTelephoneAndPassword(telephone, hashPassword);
        if(userEntity==null){
            return RestResult.fail().message("用户名或密码错误");
        }

        Map<String,Object> param=new HashMap<>();
        param.put("userId",userEntity.getUserId());
        String token="";
        try{
            token=jwtComp.createJWT(param);
        }catch (Exception e){
            log.info("登陆失败：{}");
            return RestResult.fail().message("创建token失败");
        }
        UserEntity sessionUserEntity=this.findUserInfoByTelephone(telephone);
        if(sessionUserEntity.getAvailable() != null && sessionUserEntity.getAvailable() == 0){
            return RestResult.fail().message("用户已被禁用");
        }
        UserLoginVO userLoginVO=new UserLoginVO();
        BeanUtil.copyProperties(sessionUserEntity,userLoginVO);
        userLoginVO.setToken(token);
        loginVo.setData(userLoginVO);
        loginVo.setCode(200001);
        loginVo.setSuccess(true);
        return loginVo;
    }


    @Override
    public UserEntity findUserInfoByTelephone(String telephone) {
        LambdaQueryWrapper<UserEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getTelephone,telephone);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public List<Role> selectRoleListByUserId(String userId) {
        return userMapper.selectRoleListByUserId(userId);
    }

    @Override
    public String getSaltByTelephone(String telephone) {
        return userMapper.getSaltByTelephone(telephone);
    }

    @Override
    public UserEntity selectUserByTelephoneAndPassword(String telephone, String password) {
        LambdaQueryWrapper<UserEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getTelephone,telephone).eq(UserEntity::getPassword,password);
        return userMapper.selectOne(wrapper);
    }


    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity userEntity = userMapper.selectById(Long.valueOf(userId));
        if(userEntity == null){
            throw new UsernameNotFoundException(String.format("用户不存在"));
        }
        List<Role> roleList = this.selectRoleListByUserId(userEntity.getUserId());
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roleList) {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_" + role.getRoleName());
            authorities.add(simpleGrantedAuthority);
        }

        JwtUser jwtUser = new JwtUser(userEntity.getUserId(), userEntity.getUsername(), userEntity.getPassword()
                ,userEntity.getAvailable(), authorities);
        return jwtUser;
    }

}
