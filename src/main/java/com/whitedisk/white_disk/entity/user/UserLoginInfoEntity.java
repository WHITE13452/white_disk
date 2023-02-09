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
@TableName("userlogininfo")
public class UserLoginInfoEntity {
    @TableId(type = IdType.AUTO)
    private Long userLoginId;
    @TableField(value = "userloginDate")
    private String userloginDate;
    @TableField(value = "userId")
    private String userId;
}
