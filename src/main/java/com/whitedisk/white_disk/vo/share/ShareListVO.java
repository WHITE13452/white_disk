package com.whitedisk.white_disk.vo.share;

import lombok.Data;

/**
 * @author white
 */
@Data
public class ShareListVO {
    private Long shareId;
    private Long userId;
    private String shareTime;
    private String endTime;
    private String extractionCode;
    private String shareBatchNum;
    /**
     * 0公共，1私密，2好友
     */
    private Integer shareType;
    /**
     * 0正常，1已失效，2已撤销
     */
    private Integer shareStatus;
    private Long shareFileId;
    private String userFileId;
    private String shareFilePath;
    private String fileId;
    private String fileName;
    private String filePath;
    private String extendName;
    private Integer isDir;
    private String uploadTime;
    private Integer deleteFlag;
    private String deleteTime;
    private String deleteBatchNum;
    private String timeStampName;
    private String fileUrl;
    private Long fileSize;
    private Integer storageType;
}
