package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.OperationLogBean;
import com.whitedisk.white_disk.mapper.OperationLogMapper;
import com.whitedisk.white_disk.service.api.IOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author white
 */
@Service
@Transactional(rollbackFor=Exception.class)
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLogBean> implements IOperationLogService {
    @Resource
    OperationLogMapper operationLogMapper;

    @Override
    public void insertOperationLog(OperationLogBean operationlogBean) {
        operationLogMapper.insert(operationlogBean);
    }
}
