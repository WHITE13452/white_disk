package com.whitedisk.white_disk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

/**
 * @author white
 */
@Data
@TableName("image")
public class Image {

    @TableId(type = IdType.AUTO)
    private Long imageId;

    private String fileId;

    private Integer imageWidth;

    private Integer imageHeight;
}
