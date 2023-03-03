package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.StorageEntity;
import com.whitedisk.white_disk.entity.SysParam;
import com.whitedisk.white_disk.mapper.StorageMapper;
import com.whitedisk.white_disk.mapper.SysParamMapper;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class StorageService extends ServiceImpl<StorageMapper, StorageEntity> implements IStorageService {

    @Resource
    private StorageMapper storageMapper;
    @Resource
    SysParamMapper sysParamMapper;
    @Resource
    UserFileMapper userFileMapper;

    @Override
    public boolean checkStorage(String userId, Long fileSize) {
        LambdaQueryWrapper<StorageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageEntity::getUserId, userId);

        StorageEntity storageEntity = storageMapper.selectOne(wrapper);
        Long totalStorageSize=null;

        if (storageEntity == null || storageEntity.getTotalStorageSize() == null) {
            LambdaQueryWrapper<SysParam> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(SysParam::getSysParamKey,"totalStorageSize");
            SysParam sysParam = sysParamMapper.selectOne(wrapper1);
            totalStorageSize= Long.parseLong(sysParam.getSysParamValue());

            storageEntity = new StorageEntity();
            storageEntity.setUserId(userId);
            storageEntity.setTotalStorageSize(totalStorageSize);

            storageMapper.insert(storageEntity);
        }else{
            totalStorageSize = storageEntity.getTotalStorageSize();
        }

        if (totalStorageSize != null) {
            totalStorageSize = totalStorageSize * 1024 * 1024;
        }

        Long storageSize = userFileMapper.selectStorageSizeByUserId(userId);
        if (storageSize == null ){
            storageSize = 0L;
        }
        if (storageSize + fileSize > totalStorageSize) {
            return false;
        }
        return true;
    }
}
