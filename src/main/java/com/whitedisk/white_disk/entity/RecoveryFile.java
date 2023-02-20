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
@TableName("recoveryfile")
public class RecoveryFile {

    @TableId(type = IdType.AUTO)
    private Long recoveryFileId;
    @TableField(value = "userFileId")
    private String userFileId;
    @TableField(value = "deleteTime")
    private String deleteTime;
    @TableField(value = "deleteBatchNum")
    private String deleteBatchNum;
}
