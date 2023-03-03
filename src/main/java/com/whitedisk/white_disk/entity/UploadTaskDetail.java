package com.whitedisk.white_disk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

@Data
@TableName("uploadtaskdetail")
public class UploadTaskDetail {

    @TableId(type = IdType.AUTO)
    private Long uploadTaskDetailId;

    private String filePath;

    private String filename;

    private int chunkNumber;

    private Integer chunkSize;

    private String relativePath;

    private Integer totalChunks;

    private Integer totalSize;

    private String identifier;
}
