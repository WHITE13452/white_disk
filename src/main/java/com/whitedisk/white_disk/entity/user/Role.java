package com.whitedisk.white_disk.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author white
 */
@Data
@TableName("role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long roleId; // 编号
    @TableField(value = "roleName")
    private String roleName;
    @TableField(value = "description")
    private String description;
    @TableField(value = "available")
    private Integer available; // 是否可用,如果不可用将不会添加给用户
    @TableField(value = "createTime")
    private String createTime;
    @TableField(value = "createUserId")
    private Long createUserId;
    @TableField(value = "modifyTime")
    private String modifyTime;
    @TableField(value = "modifyUserId")
    private Long modifyUserId;
}
