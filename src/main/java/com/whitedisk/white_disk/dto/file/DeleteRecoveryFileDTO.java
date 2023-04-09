package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
@Schema(name = "删除回收文件DTO",required = true)
public class DeleteRecoveryFileDTO {
    @Schema(description = "用户文件id", required = true)
    private String userFileId;
}
