package com.whitedisk.white_disk.entity;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qiwenshare.common.util.DateUtil;
import com.whitedisk.white_disk.utils.WhiteFile;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author white
 */
@Data
@TableName("userfile")
public class UserFileEntity {
    @TableId(type = IdType.AUTO)
    private String userFileId;

    @TableField(value = "userId")
    private String userId;

    @TableField(value = "fileId")
    private String fileId;

    @TableField(value = "fileName")
    private String fileName;

    @TableField(value = "filePath")
    private String filePath;

    @TableField(value = "extendName")
    private String extendName;

    @TableField(value = "isDir")
    private Integer isDir;//是否是目录

    @TableField(value = "uploadTime")
    private String uploadTime;

    @TableField(value = "deleteFlag")
    private Integer deleteFlag;

    @TableField(value = "deleteTime")
    private String deleteTime;

    @TableField(value = "deleteBatchNum")
    private String deleteBatchNum;

    public UserFileEntity() {};
    public UserFileEntity(WhiteFile whiteFile, String userId, String fileId) {
        this.userFileId = IdUtil.getSnowflakeNextIdStr();
        this.userId = userId;
        this.fileId = fileId;
        this.filePath = whiteFile.getParent();
        this.fileName = whiteFile.getNameNotExtend();
        this.extendName = whiteFile.getExtendName();
        this.isDir = whiteFile.isDirectory() ? 1 : 0;
        this.uploadTime = DateUtil.getCurrentTime();
        this.deleteFlag = 0;
    }

    public boolean isDirectory() {
        return this.isDir == 1;
    }

    public boolean isFile() {
        return this.isDir == 0;
    }
}
