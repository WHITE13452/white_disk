package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
@Schema(name = "批量下载文件DTO",required = true)
public class BatchDownloadFileDTO {
    @Schema(description="文件集合", required = true)
    private String userFileIds;
}
