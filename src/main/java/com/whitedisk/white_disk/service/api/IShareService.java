package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whitedisk.white_disk.dto.file.ShareListDTO;
import com.whitedisk.white_disk.entity.Share;
import com.whitedisk.white_disk.vo.share.ShareListVO;

import java.util.List;

/**
 * @author white
 */
public interface IShareService extends IService<Share> {
    List<ShareListVO> selectShareList(ShareListDTO shareListDTO, String userId);
    int selectShareListTotalCount(ShareListDTO shareListDTO, String userId);
}
