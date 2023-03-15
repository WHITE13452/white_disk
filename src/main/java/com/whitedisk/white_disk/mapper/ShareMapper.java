package com.whitedisk.white_disk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whitedisk.white_disk.entity.Share;
import com.whitedisk.white_disk.vo.share.ShareListVO;

import java.util.List;

/**
 * @author white
 */
public interface ShareMapper extends BaseMapper<Share> {
    List<ShareListVO> selectShareList(String shareFilePath, String shareBatchNum, Long beginCount, Long pageCount, String userId);
    int selectShareListTotalCount(String shareFilePath,String shareBatchNum, String userId);
}
