package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiwenshare.common.util.security.JwtUser;
import com.whitedisk.white_disk.dto.file.CreateFileDTO;
import com.whitedisk.white_disk.dto.file.RenameFileDTO;
import com.whitedisk.white_disk.dto.file.SearchFileDTO;
import com.whitedisk.white_disk.entity.FileEntity;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.entity.user.UserEntity;
import com.whitedisk.white_disk.vo.file.SearchFileVO;
import org.apache.xpath.operations.Bool;

import javax.annotation.processing.FilerException;
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
    Boolean createFile(CreateFileDTO createFileDTO, JwtUser user);

    /**
     * 搜索文件接口
     * @param searchFileDTO
     * @param user
     * @return
     */
    List<SearchFileVO> searchFile(SearchFileDTO searchFileDTO,  JwtUser user);

    /**
     * 重命名文件
     * @param renameFileDto
     * @param user
     * @return
     */
    Boolean renameFile(RenameFileDTO renameFileDto, JwtUser user);

}