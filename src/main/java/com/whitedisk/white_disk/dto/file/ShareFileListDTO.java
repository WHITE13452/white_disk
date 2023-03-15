package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
public class ShareFileListDTO {
    @Schema(description="批次号")
    private String shareBatchNum;
    @Schema(description="分享文件路径")
    private String shareFilePath;
}
