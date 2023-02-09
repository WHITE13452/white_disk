package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whitedisk.white_disk.entity.UserFileEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author white
 */
public interface IUserFileService extends IService<UserFileEntity> {
    List<UserFileEntity> selectUserFileByNameAndPath(String fileName, String filePath, String userId);
    List<UserFileEntity> selectUserFileByLikeRightFilePath(String filePath, String userId);

}
