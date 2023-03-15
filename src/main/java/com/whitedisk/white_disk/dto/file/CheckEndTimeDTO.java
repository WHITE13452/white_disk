package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
@Schema(name = "校验过期时间DTO",required = true)
public class CheckEndTimeDTO {
    @Schema(description="批次号")
    private String shareBatchNum;

}
