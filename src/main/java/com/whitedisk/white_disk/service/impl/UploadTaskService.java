package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.UploadTask;
import com.whitedisk.white_disk.mapper.UploadTaskMapper;
import com.whitedisk.white_disk.service.api.IUploadTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author white
 */
@Service
@Slf4j
public class UploadTaskService extends ServiceImpl<UploadTaskMapper, UploadTask> implements IUploadTaskService {
}
