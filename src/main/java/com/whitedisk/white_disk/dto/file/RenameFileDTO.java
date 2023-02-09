package com.whitedisk.white_disk.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author white
 */
@Data
@Schema(name = "重命名文件DTO",required = true)
public class RenameFileDTO {
    @Schema(description = "用户文件id", required = true)
    private String userFileId;

    @Schema(description = "文件名", required = true)
    private String fileName;
}
