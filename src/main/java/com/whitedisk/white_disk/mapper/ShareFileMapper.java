package com.whitedisk.white_disk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whitedisk.white_disk.entity.ShareFile;
import com.whitedisk.white_disk.vo.file.ShareFileListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author white
 */
public interface ShareFileMapper extends BaseMapper<ShareFile> {
    List<ShareFileListVO> selectShareFileList(@Param("shareBatchNum") String shareBatchNum, @Param("shareFilePath") String filePath);
    void batchInsertShareFile(List<ShareFile> shareFiles);
}
