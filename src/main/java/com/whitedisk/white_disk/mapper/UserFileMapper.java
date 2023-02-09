package com.whitedisk.white_disk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.vo.file.FileListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author white
 */
public interface UserFileMapper extends BaseMapper<UserFileEntity> {
    List<UserFileEntity> selectUserFileByLikeRightFilePath(@Param("filePath") String filePath, @Param("userId") String userId);
    IPage<FileListVO> selectPageVo(Page<?> page, @Param("userFile") UserFileEntity userFile, @Param("fileTypeId") Integer fileTypeId);

}
