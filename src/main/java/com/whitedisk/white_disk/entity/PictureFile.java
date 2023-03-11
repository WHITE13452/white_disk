package com.whitedisk.white_disk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("picturefile")
public class PictureFile {

    @TableId(type = IdType.AUTO)
    private Long pictureFileId;

    private String fileUrl;

    private Long fileSize;

    private Integer storageType;

    private Long userId;

    private String fileName;

    private String extendName;

    private String createTime;

    private Long createUserId;

    private String modifyTime;

    private Long modifyUserId;

}
