package com.whitedisk.white_disk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whitedisk.white_disk.entity.UserFileEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author white
 */
public interface UserFileMapper extends BaseMapper<UserFileEntity> {
    List<UserFileEntity> selectUserFileByLikeRightFilePath(@Param("filePath") String filePath, @Param("userId") String userId);
}
