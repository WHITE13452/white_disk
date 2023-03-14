package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiwenshare.common.util.security.JwtUser;
import com.whitedisk.white_disk.dto.file.*;
import com.whitedisk.white_disk.entity.FileEntity;
import com.whitedisk.white_disk.vo.file.FileDetailVO;
import com.whitedisk.white_disk.vo.file.SearchFileVO;

import java.util.List;

/**
 * @author white
 */
public interface IFileService extends IService<FileEntity> {

    /**
     * 创建文件接口
     * @param createFileDTO
     * @param user
     * @return
     */
    String createFile(CreateFileDTO createFileDTO,JwtUser user);
    /**
     * 创建文件接口
     * @param createFileDTO
     * @param user
     * @return
     */
    Boolean createFold(CreateFoldDTO createFileDTO, JwtUser user);

    /**
     * 搜索文件接口
     * @param searchFileDTO
     * @param user
     * @return
     */
    List<SearchFileVO> searchFile(SearchFileDTO searchFileDTO, JwtUser user);

    /**
     * 重命名文件
     * @param renameFileDto
     * @param user
     * @return
     */
    Boolean renameFile(RenameFileDTO renameFileDto, JwtUser user);

    /**
     * 解压文件接口
     * @param userFileId
     * @param unzipMode
     * @param filePath
     */
    void unzipFile(String userFileId, int unzipMode, String filePath);

    /**
     * 复制文件接口
     * @param copyFileDTO
     * @param user
     * @return
     */
    Boolean copyFile(CopyFileDTO copyFileDTO, JwtUser user);

    /**
     * 文件详情
     * @param userFileId
     * @return
     */
    FileDetailVO getFileDetail(String userFileId);

    /**
     * 文件被引用次数
     * @param fileId
     * @return
     */
    Long getFilePointCount(String fileId);

    /**
     * 更新文本文件内容
     * @param userFileId
     * @param identifier
     * @param fileSize
     */
    void updateFileDetail(String userFileId, String identifier, long fileSize);

}
