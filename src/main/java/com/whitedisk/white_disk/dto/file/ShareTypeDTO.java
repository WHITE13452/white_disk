package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
public class ShareTypeDTO {
    @Schema(description="批次号")
    private String shareBatchNum;

}
