package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
public class ShareListDTO {
    @Schema(description="分享文件路径")
    private String shareFilePath;
    @Schema(description="批次号")
    private String shareBatchNum;
    @Schema(description = "当前页码")
    private Long currentPage;
    @Schema(description = "一页显示数量")
    private Long pageCount;
}
