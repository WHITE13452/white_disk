package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.ShareFile;
import com.whitedisk.white_disk.mapper.ShareFileMapper;
import com.whitedisk.white_disk.service.api.IShareFileService;
import com.whitedisk.white_disk.vo.file.ShareFileListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author white
 */
@Slf4j
@Service
@Transactional(rollbackFor=Exception.class)
public class ShareFileService extends  ServiceImpl<ShareFileMapper, ShareFile> implements IShareFileService {
    @Resource
    ShareFileMapper shareFileMapper;

    @Override
    public List<ShareFileListVO> selectShareFileList(String shareBatchNum, String filePath) {
        return shareFileMapper.selectShareFileList(shareBatchNum, filePath);
    }
}