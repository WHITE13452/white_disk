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
@TableName("operationlog")
public class OperationLogBean {

    @TableId(type = IdType.AUTO)
    private Long operationLogId;

    private String userId;

    private String operation;

    private String operationObj;

    private String terminal;

    private String result;

    private String detail;

    private String source;

    private String time;

    private Integer logLevel;

    private Integer platform;

    private String requestURI;
    private String requestMethod;
}
