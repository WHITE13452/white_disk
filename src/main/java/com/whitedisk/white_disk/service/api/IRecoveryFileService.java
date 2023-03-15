package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whitedisk.white_disk.entity.RecoveryFile;
import com.whitedisk.white_disk.vo.file.RecoveryFileListVo;

import java.util.List;

/**
 * @author white
 */
public interface IRecoveryFileService extends IService<RecoveryFile> {
    List<RecoveryFileListVo> selectRecoveryFileList(String userId);
}
