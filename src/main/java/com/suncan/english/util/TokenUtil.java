package com.suncan.english.util;

import com.suncan.english.config.TokenProperties;
import com.suncan.english.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 工具类，负责 token 的签发和解析。
 */
@Component
public class TokenUtil {

    private final TokenProperties tokenProperties;

    public TokenUtil(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    public String createToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(tokenProperties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(getKey())
                .compact();
    }

    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            throw new BusinessException("token 无效或已过期");
        }
    }

    private SecretKey getKey() {
        String secret = tokenProperties.getSecret();
        if (secret == null || secret.length() < 32) {
            throw new BusinessException("token.secret 长度至少 32 位");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
