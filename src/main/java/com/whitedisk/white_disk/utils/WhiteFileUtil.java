package com.whitedisk.white_disk.utils;

import cn.hutool.core.util.IdUtil;
import com.qiwenshare.common.util.DateUtil;
import com.whitedisk.white_disk.entity.UserFileEntity;

/**
 * @author white
 */
public class WhiteFileUtil {

    public static UserFileEntity getWhiteDir(String userId, String filePath, String fileName) {
        UserFileEntity userFile = new UserFileEntity();
        userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
        userFile.setUserId(userId);
        userFile.setFileId(null);
        userFile.setFileName(fileName);
        userFile.setFilePath(WhiteFile.formatPath(filePath));
        userFile.setExtendName(null);
        userFile.setIsDir(1);
        userFile.setUploadTime(DateUtil.getCurrentTime());
        userFile.setDeleteFlag(0);
        userFile.setDeleteBatchNum(null);
        return userFile;
    }

    public static UserFileEntity getWhiteFile(String userId, String fileId, String filePath, String fileName, String extendName) {
        UserFileEntity userFile = new UserFileEntity();
        userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
        userFile.setUserId(userId);
        userFile.setFileId(fileId);
        userFile.setFileName(fileName);
        userFile.setFilePath(WhiteFile.formatPath(filePath));
        userFile.setExtendName(extendName);
        userFile.setIsDir(0);
        userFile.setUploadTime(DateUtil.getCurrentTime());
        userFile.setDeleteFlag(0);
        userFile.setDeleteBatchNum(null);
        return userFile;
    }

    public static UserFileEntity searchWhiteFileParam(UserFileEntity userFile) {
        UserFileEntity param = new UserFileEntity();
        param.setFilePath(WhiteFile.formatPath(userFile.getFilePath()));
        param.setFileName(userFile.getFileName());
        param.setExtendName(userFile.getExtendName());
        param.setDeleteFlag(0);
        param.setUserId(userFile.getUserId());
        param.setIsDir(0);
        return param;
    }

    public static String formatLikePath(String filePath) {
        String newFilePath = filePath.replace("'", "\\'");
        newFilePath = newFilePath.replace("%", "\\%");
        newFilePath = newFilePath.replace("_", "\\_");
        return newFilePath;
    }
}
