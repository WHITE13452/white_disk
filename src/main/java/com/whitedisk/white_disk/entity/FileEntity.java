package com.whitedisk.white_disk.entity;

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
@TableName("file")
public class FileEntity {

    @TableId(type = IdType.AUTO)
    private String fileId;

    @TableField(value = "fileUrl")
    private String fileUrl;

    @TableField(value = "fileSize")
    private Long fileSize;

    @TableField(value = "fileStatus")
    private Integer fileStatus;//0-失效，1-生效

    @TableField(value = "storageType")
    private Integer storageType;//存储类型

    @TableField(value = "identifier")
    private String identifier;//md5唯一标识

    @TableField(value = "createTime")
    private String createTime;

    @TableField(value = "createUserId")
    private String createUserId;

    @TableField(value = "modifyTime")
    private String modifyTime;

    @TableField(value = "modifyUserId")
    private String modifyUserId;
}
