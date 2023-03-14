package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
@Schema(name = "分享文件DTO",required = true)
public class ShareFileDTO {
    @Schema(description="文件集合")
    private String files;
    @Schema(description = "过期日期", example="2023-03-15 22:10:33")
    private String endTime;
    @Schema(description = "分享类型", example="0公共分享，1私密分享，2好友分享")
    private Integer shareType;
    @Schema(description = "备注", example="")
    private String remarks;

}
