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
@TableName("sharefile")
public class ShareFile {

    @TableId(type = IdType.AUTO)
    private Long shareFileId;

    private String shareBatchNum;

    private String userFileId;

    private String shareFilePath;
}
