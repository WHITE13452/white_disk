package com.whitedisk.white_disk.controller;

import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.dto.user.RegisterDTO;
import com.whitedisk.white_disk.service.api.IUserLoginInfoService;
import com.whitedisk.white_disk.service.api.IUserService;
import com.whitedisk.white_disk.vo.user.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
        return userService.userLogin(telephone, password);
    }

    @Operation(summary = "检查用户信息", description = "验证用户token", tags = {"user"})
    @GetMapping("/checkLoginUserInfo")
    @ResponseBody
    public RestResult<UserLoginVO> checkLoginUserInfo(){
        JwtUser sessionUserEntity = SessionUtil.getSession();
        return userLoginInfoService.checkLoginUserInfo(sessionUserEntity);
    }


}
