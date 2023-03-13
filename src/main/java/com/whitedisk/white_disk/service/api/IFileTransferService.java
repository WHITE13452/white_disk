package com.whitedisk.white_disk.service.api;

import com.qiwenshare.common.util.security.JwtUser;
import com.whitedisk.white_disk.dto.file.DownloadFileDTO;
import com.whitedisk.white_disk.dto.file.PreviewDTO;
import com.whitedisk.white_disk.dto.file.UploadFileDTO;
import com.whitedisk.white_disk.entity.StorageEntity;
import com.whitedisk.white_disk.vo.file.UploadFileVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author white
 */
public interface IFileTransferService {
    UploadFileVo uploadFileSpeed(UploadFileDTO uploadFileDTO);

    void uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO, String userId);

    StorageEntity getStorage(JwtUser user);

    void previewPictureFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO);

    void previewFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO);

    void downloadFile(HttpServletResponse httpServletResponse, DownloadFileDTO downloadFileDTO);

    void downloadUserFileList(HttpServletResponse httpServletResponse, String filePath, String fileName, List<String> userFileIds);

}
