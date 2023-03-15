package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.dto.file.ShareListDTO;
import com.whitedisk.white_disk.entity.Share;
import com.whitedisk.white_disk.mapper.ShareMapper;
import com.whitedisk.white_disk.service.api.IShareService;
import com.whitedisk.white_disk.vo.share.ShareListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class ShareService extends ServiceImpl<ShareMapper, Share> implements IShareService {
    @Resource
    private ShareMapper shareMapper;


    @Override
    public List<ShareListVO> selectShareList(ShareListDTO shareListDTO, String userId) {
        long beginCount = (shareListDTO.getCurrentPage() - 1) * shareListDTO.getPageCount();
        return shareMapper.selectShareList(shareListDTO.getShareFilePath(),
                shareListDTO.getShareBatchNum(), beginCount,
                shareListDTO.getPageCount(), userId);
    }

    @Override
    public int selectShareListTotalCount(ShareListDTO shareListDTO, String userId) {
        return 0;
    }
}
