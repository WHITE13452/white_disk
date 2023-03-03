package com.whitedisk.white_disk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author white
 */
@Data
@TableName("storage")
public class StorageEntity {

    @TableId(type = IdType.AUTO)
    private Long storageId;

    private String userId;

    private Long storageSize;

    private Long totalStorageSize;

    private String modifyTime;

    private Long modifyUserId;

    public StorageEntity() {

    }

    public StorageEntity(String userId) {
        this.userId = userId;
    }
}
