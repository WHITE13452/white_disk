package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.whitedisk.white_disk.entity.user.UserLoginInfoEntity;
import com.whitedisk.white_disk.vo.user.UserLoginVO;

/**
 * @author white
 */
public interface IUserLoginInfoService extends IService<UserLoginInfoEntity> {
    /**
     * 验证用户登录token
     * @return
     */
    RestResult<UserLoginVO> checkLoginUserInfo(JwtUser sessionUserEntity);
}
