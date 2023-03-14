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
    /**
     * 分享类型(0公共,1私密,2好友)
     */
    private Integer shareType;
    /**
     * 分享状态(0正常,1已失效,2已撤销)
     */
    private Integer shareStatus;
}
