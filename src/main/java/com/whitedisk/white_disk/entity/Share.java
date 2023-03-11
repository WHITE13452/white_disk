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
@TableName("share")
public class Share {

    @TableId(type = IdType.AUTO)
    private Long shareId;

    private String userId;

    private String shareTime;

    private String endTime;

    private String extractionCode;

    private String shareBatchNum;

    private Integer shareType;

    private Integer shareStatus;
}
