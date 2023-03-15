package com.whitedisk.white_disk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whitedisk.white_disk.entity.RecoveryFile;
import com.whitedisk.white_disk.vo.file.RecoveryFileListVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author white
 */
public interface RecoveryFileMapper extends BaseMapper<RecoveryFile> {
    List<RecoveryFileListVo> selectRecoveryFileList(@Param("userId") String userId);
}
