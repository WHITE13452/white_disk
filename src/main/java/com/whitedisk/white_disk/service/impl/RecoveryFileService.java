package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.RecoveryFile;
import com.whitedisk.white_disk.mapper.RecoveryFileMapper;
import com.whitedisk.white_disk.service.api.IRecoveryFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class RecoveryFileService extends ServiceImpl<RecoveryFileMapper,RecoveryFile> implements IRecoveryFileService {
}
