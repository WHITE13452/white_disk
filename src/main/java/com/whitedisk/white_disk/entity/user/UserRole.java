package com.whitedisk.white_disk.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author white
 */
@Data
@TableName("user_role")
public class UserRole {
    @TableId(type = IdType.AUTO)
    private Long userRoleId;
    @TableField(value = "userId")
    private String userId;
    @TableField(value = "roleId")
    private Long roleId;
}
