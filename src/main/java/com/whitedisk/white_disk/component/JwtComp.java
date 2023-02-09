package com.whitedisk.white_disk.component;

import com.alibaba.fastjson2.JSON;
import com.qiwenshare.common.util.math.CalculatorUtils;
import com.whitedisk.white_disk.config.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.apache.commons.net.util.Base64;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.kerberos.KerberosKey;
import java.util.Date;
import java.util.Map;

/**
 * @author white
 */
@Component
public class JwtComp {

    @Resource
    JwtProperties jwtProperties;

    /**
     * 生成加密key
     * @return
     */
    private SecretKey generalKey(){
        byte[] encodeKey = Base64.decodeBase64(jwtProperties.getSecret());

        SecretKey key = new SecretKeySpec(encodeKey,0, encodeKey.length, "AES");
        return key;
    }

    /**
     * 创建jwt
     * @param param
     * @return
     */
    public String createJWT(Map<String,Object> param){
        String subject = JSON.toJSONString(param);
        //生成jwt的时间
        long nowTime = System.currentTimeMillis();
        Date nowDate = new Date(nowTime);
        SecretKey key = generalKey();
        Double expTime = CalculatorUtils.conversion(jwtProperties.getPayload().getRegisterdClaims().getExp());
        //为payload添加标准声名和私有声明
        DefaultClaims defaultClaims = new DefaultClaims();
        defaultClaims.setIssuer(jwtProperties.getPayload().getRegisterdClaims().getIss());
        defaultClaims.setExpiration(new Date(System.currentTimeMillis() + expTime.longValue()));
        defaultClaims.setSubject(subject);
        defaultClaims.setAudience(jwtProperties.getPayload().getRegisterdClaims().getAud());

        JwtBuilder jwtBuilder= Jwts.builder()
                .setClaims(defaultClaims)
                .setIssuedAt(nowDate)
                .signWith(SignatureAlgorithm.forName(jwtProperties.getHeader().getAlg()),key);
        return jwtBuilder.compact();
    }

    /**
     * 解密jwt
     * @param jwt
     * @return
     */
    public Claims parseJWT(String jwt){
        SecretKey key = generalKey();   //拿到签名密钥
        Claims claims = Jwts.parser() //得到DefaultJwtParser
                .setSigningKey(key)//设置签名密钥
                .parseClaimsJws(jwt)
                .getBody();//拿到要解析的jwt
        return claims;
    }
}

