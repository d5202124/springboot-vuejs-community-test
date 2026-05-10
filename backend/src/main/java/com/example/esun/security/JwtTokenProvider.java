package com.example.esun.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key secretKey;
    private final long validityInMilliseconds;

    // @Value 從 application.yml 讀取 jwt.secret 與 jwt.expiration-ms 注入
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long validityInMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    // 產生 JWT，payload 存入 userId 與 phone
    public String generateToken(Long userId, String phone) {
        Claims claims = Jwts.claims().setSubject(phone);
        claims.put("userId", userId);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 驗證 JWT 是否有效（未過期、未被竄改），任一不符拋例外回傳 false
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // 從 JWT payload 取出手機號碼（subject 欄位）
    public String getPhone(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 從 JWT payload 取出 userId
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }
}
