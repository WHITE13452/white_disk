package com.whitedisk.white_disk.component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiwenshare.common.constant.RegexConstant;
import com.whitedisk.white_disk.entity.user.UserEntity;
import com.whitedisk.white_disk.mapper.UserMapper;
import kotlin.jvm.internal.Lambda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author white
 */
@Component
public class UserDealComp {

    @Resource
    private UserMapper userMapper;

    /**
     * 用户是否存在
     * @param userEntity
     * @return
     */
    public  Boolean isUserNameExit(UserEntity userEntity){
        LambdaQueryWrapper<UserEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername,userEntity.getUsername());
        List<UserEntity> list=userMapper.selectList(wrapper);
        if(!list.isEmpty() && list != null){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 手机号是否存在
     * @param userEntity
     * @return
     */
    public Boolean isPhoneExit(UserEntity userEntity){
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getTelephone, userEntity.getTelephone());
        List<UserEntity> list = userMapper.selectList(wrapper);
        if (list != null && !list.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 手机号格式是否正确
     * @param phone
     * @return
     */
    public Boolean isPhoneFormatRight(String phone){
        boolean isRight = Pattern.matches(RegexConstant.PASSWORD_REGEX, phone);
        return isRight;
    }
}
