package com.whitedisk.white_disk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

/**
 * @author white
 */
@Data
@TableName("sysparam")
public class SysParam {
    @TableId(type = IdType.AUTO)
    private Long sysParamId;
    @TableField(value = "groupName")
    private String groupName;
    @TableField(value = "sysParamKey")
    private String sysParamKey;
    @TableField(value = "sysParamValue")
    private String sysParamValue;
    @TableField(value = "sysParamDesc")
    private String sysParamDesc;
}
