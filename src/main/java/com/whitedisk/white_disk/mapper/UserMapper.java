package com.whitedisk.white_disk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whitedisk.white_disk.entity.user.Role;
import com.whitedisk.white_disk.entity.user.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author white
 */

public interface UserMapper extends BaseMapper<UserEntity> {

    int inserUser(UserEntity userEntity);
    List<Role> selectRoleListByUserId(@Param("userId") String userId);
    String getSaltByTelephone(@Param("telephone") String telephone);
    int insertUserRole(@Param("userId") String userId, @Param("roleId") long roleId);

}
