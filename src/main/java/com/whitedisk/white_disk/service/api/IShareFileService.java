package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whitedisk.white_disk.entity.ShareFile;
import com.whitedisk.white_disk.vo.file.ShareFileListVO;

import java.util.List;

/**
 * @author white
 */
public interface IShareFileService extends IService<ShareFile> {
    List<ShareFileListVO> selectShareFileList(String shareBatchNum, String filePath);
    void batchInsertShareFile(List<ShareFile> shareFiles);
}
