package com.whitedisk.white_disk.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.HashUtils;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.JwtComp;
import com.whitedisk.white_disk.dto.user.RegisterDTO;
import com.whitedisk.white_disk.entity.user.UserEntity;
import com.whitedisk.white_disk.entity.user.UserLoginInfoEntity;
import com.whitedisk.white_disk.service.api.IUserLoginInfoService;
import com.whitedisk.white_disk.service.api.IUserService;
import com.whitedisk.white_disk.vo.user.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * @author white
 * 用户操作
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserLoginInfoService userLoginInfoService;
    @Resource
    JwtComp jwtComp;

    public static final String CURRENT_MODULE = "用户管理";

    @Operation(summary = "用户注册", description = "注册账号", tags = {"user"})
    @MyLog(operation = "用户注册", module = CURRENT_MODULE)
    @PostMapping("/register")
    @ResponseBody
    public RestResult<String>   addUser(@Valid @RequestBody RegisterDTO registerDTO){
        RestResult<String> result=null;
        result=userService.registerUser(registerDTO);
        return result;
    }

    @Operation(summary = "用户登录", description = "登录", tags = {"user"})
    @MyLog(operation = "用户登录", module = CURRENT_MODULE)
    @GetMapping("/login")
    @ResponseBody
    public RestResult<UserLoginVO> userLogin(@Parameter(description = "登录手机号") String telephone, @Parameter(description = "登录密码") String password){
        RestResult<UserLoginVO> restResult = new RestResult<UserLoginVO>();
        String salt = userService.getSaltByTelephone(telephone);
        String hashPassword = HashUtils.hashHex("MD5", password, salt, 1024);
        UserEntity result = userService.selectUserByTelephoneAndPassword(telephone, hashPassword);
        if (result == null) {
            return RestResult.fail().message("手机号或密码错误！");
        }

        Map<String, Object> param = new HashMap<>();
        param.put("userId", result.getUserId());
        String token = "";
        try {
            token = jwtComp.createJWT(param);
        } catch (Exception e) {
            log.info("登录失败：{}", e);
            return RestResult.fail().message("创建token失败！");
        }
        UserEntity sessionUserBean = userService.findUserInfoByTelephone(telephone);
        if (sessionUserBean.getAvailable() != null && sessionUserBean.getAvailable() == 0) {
            return RestResult.fail().message("用户已被禁用");
        }
        UserLoginVO userLoginVo = new UserLoginVO();
        BeanUtil.copyProperties(sessionUserBean, userLoginVo);
        userLoginVo.setToken("Bearer " + token);
        restResult.setData(userLoginVo);
        restResult.setSuccess(true);
        restResult.setCode(200001);
        return restResult;
    }

    @Operation(summary = "检查用户信息", description = "验证用户token", tags = {"user"})
    @GetMapping("/checkuserlogininfo")
    @ResponseBody
    public RestResult<UserLoginVO> checkLoginUserInfo(){
        JwtUser sessionUserEntity = SessionUtil.getSession();
        System.out.println(sessionUserEntity);
        UserLoginVO userLoginVO = new UserLoginVO();

        if (sessionUserEntity != null && !"anonymousUser".equals(sessionUserEntity.getUsername())) {
            LambdaQueryWrapper<UserLoginInfoEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserLoginInfoEntity::getUserId,sessionUserEntity.getUserId());
            wrapper.likeRight(UserLoginInfoEntity::getUserloginDate, DateUtil.getCurrentTime().substring(0, 10));
            userLoginInfoService.remove(wrapper);
            UserLoginInfoEntity userLoginInfo = new UserLoginInfoEntity();
            userLoginInfo.setUserId(sessionUserEntity.getUserId());
            userLoginInfo.setUserloginDate(DateUtil.getCurrentTime());
            userLoginInfoService.save(userLoginInfo);
            UserEntity user = userService.getById(sessionUserEntity.getUserId());
            BeanUtils.copyProperties(user, userLoginVO);
//            if (StringUtils.isEmpty(user.getWxOpenId())) {
//                userLoginVO.setHasWxAuth(false);
//            } else {
//                userLoginVO.setHasWxAuth(true);
//            }
            return RestResult.success().data(userLoginVO);
        } else {
            return RestResult.fail().message("用户暂未登录");
        }
    }

}
