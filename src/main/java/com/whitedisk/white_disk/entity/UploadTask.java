package com.whitedisk.white_disk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

@Data
@TableName("uploadtask")
public class UploadTask {

    @TableId(type = IdType.AUTO)
    private Long uploadTaskId;

    private String userId;

    private String identifier;

    private String fileName;

    private String filePath;

    private String extendName;

    private String uploadTime;

    private Integer uploadStatus;
}
