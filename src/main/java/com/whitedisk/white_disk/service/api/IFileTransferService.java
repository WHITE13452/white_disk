package com.whitedisk.white_disk.service.api;

import com.whitedisk.white_disk.dto.file.UploadFileDTO;
import com.whitedisk.white_disk.vo.file.UploadFileVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author white
 */
public interface IFileTransferService {
    UploadFileVo uploadFileSpeed(UploadFileDTO uploadFileDTO);

    void uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO, String userId);
}
