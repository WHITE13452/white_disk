package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiwenshare.common.result.RestResult;
import com.whitedisk.white_disk.dto.user.RegisterDTO;
import com.whitedisk.white_disk.entity.user.Role;
import com.whitedisk.white_disk.entity.user.UserEntity;
import com.whitedisk.white_disk.vo.user.UserLoginVO;

import java.util.List;

/**
 * @author white
 */
public interface IUserService extends IService<UserEntity> {
    /**
     * 根据id拿到token
     * @param token
     * @return
     */
    String getUserIdByToken(String token);

    /**
     * 注册用户
     * @param registerDTO
     * @return
     */
    RestResult<String>  registerUser(RegisterDTO registerDTO);

    /**
     * 登录
     * @param telephone
     * @param password
     * @return
     */
    RestResult<UserLoginVO> userLogin(String telephone,String password);

    /**
     * 根据电话号码拿到用户信息
     * @param telephone
     * @return
     */
    UserEntity findUserInfoByTelephone(String telephone);

    /**
     * 根据userid拿到权限列表
     * @param userId
     * @return
     */
    List<Role>  selectRoleListByUserId(String userId);

    /**
     * 根据账号拿到salt
     * @param telephone
     * @return
     */
    String  getSaltByTelephone(String telephone);

    /**
     * 根据电话和密码找到user
     * @param telephone
     * @param password
     * @return
     */
    UserEntity selectUserByTelephoneAndPassword(String telephone,String password);


}
