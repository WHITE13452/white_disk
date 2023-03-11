package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whitedisk.white_disk.entity.StorageEntity;

/**
 * @author white
 */
public interface IStorageService extends IService<StorageEntity> {
    /**
     * 检查存储空间
     * @param userId
     * @param fileSize
     * @return
     */
    boolean checkStorage(String userId, Long fileSize);

    Long getTotalStorageSize(String userId);
}
