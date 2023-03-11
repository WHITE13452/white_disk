package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.Share;
import com.whitedisk.white_disk.mapper.ShareMapper;
import com.whitedisk.white_disk.service.api.IShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class ShareService extends ServiceImpl<ShareMapper, Share> implements IShareService {
}
