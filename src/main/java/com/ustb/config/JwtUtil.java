package com.ustb.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(JwtProperties properties) {
        byte[] keyBytes = Base64.getDecoder().decode(properties.getSecret());
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.expiration = properties.getExpiration();
    }

    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder()  // 利用jwt官方提供的工具类Jwts来生成或解析jwt令牌，此处为生成。
                .subject(username)
                .claim("userId", userId)  // 添加自定义信息
                .claim("role", role) // 添加自定义信息
                .issuedAt(now)  // 签发时间
                .expiration(exp)  // 设置过期时间
                .signWith(secretKey)
                .compact();  // 生成令牌
    }

    public Claims parseToken(String token) {
        return Jwts.parser()  // 利用jwt官方提供的工具类Jwts来生成或解析jwt令牌，此处为解析。
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
