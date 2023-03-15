package com.whitedisk.white_disk.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * @author white
 */
@Data
@Schema(description="分享类型VO")
public class ShareTypeVO {
    @Schema(description="0公共，1私密，2好友")
    private Integer shareType;
}
