package com.whitedisk.white_disk.config.jwt;

import lombok.Data;

/**
 * @author white
 */
@Data
public class RegisterdClaims {
    private String iss;
    private String exp;
    private String sub;
    private String aud;
}
