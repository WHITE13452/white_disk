package com.whitedisk.white_disk.config.jwt;

import lombok.Data;

/**
 * @author white
 */
@Data
public class JwtHeader {
    private String alg;
    private String typ;
}
