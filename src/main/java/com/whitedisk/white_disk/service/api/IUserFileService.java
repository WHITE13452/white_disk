package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.vo.file.FileListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author white
 */
public interface IUserFileService extends IService<UserFileEntity> {
    List<UserFileEntity> selectUserFileByNameAndPath(String fileName, String filePath, String userId);
    List<UserFileEntity> selectUserFileByLikeRightFilePath(String filePath, String userId);
    List<UserFileEntity> selectSameUserFile(String fileName, String filePath, String extendName, String userId);
    IPage<FileListVO> userFileList(String userId, String filePath, Long currentPage, Long pageCount);
    IPage<FileListVO> getFileByFileType(Integer fileTypeId, Long currentPage, Long pageCount, String userId);
}
