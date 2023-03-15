package com.whitedisk.white_disk.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
@Schema(description="分享文件VO")
public class ShareFileVO {
    @Schema(description="批次号")
    private String shareBatchNum;
    @Schema(description = "提取编码")
    private String extractionCode;
}
