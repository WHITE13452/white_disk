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
@TableName("user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private String userId;
    @TableField(value = "username")
    private String username;
    @TableField(value = "password")
    private String password;
    @TableField(value = "telephone")
    private String telephone;
    @TableField(value = "email")
    private String email;
    @TableField(value = "sex")
    private String sex;
    @TableField(value = "birthday")
    private String birthday;
    @TableField(value = "addrProvince")
    private String addrProvince;
    @TableField(value = "addrCity")
    private String addrCity;
    @TableField(value = "addrArea")
    private String addrArea;
    @TableField(value = "industry")
    private String industry;
    @TableField(value = "position")
    private String position;
    @TableField(value = "intro")
    private String intro;
    @TableField(value = "salt")
    private String salt;
    @TableField(value = "imageUrl")
    private String imageUrl;
    @TableField(value = "registerTime")
    private String registerTime;
    @TableField(value = "lastLoginTime")
    private String lastLoginTime;
    @TableField(value = "available")
    private Integer available;
    @TableField(value = "modifyTime")
    private String modifyTime;
    @TableField(value = "modifyUserId")
    private Long modifyUserId;

}
