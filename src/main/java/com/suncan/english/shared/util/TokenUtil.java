package com.suncan.english.shared.util;

import com.suncan.english.shared.config.TokenProperties;
import com.suncan.english.shared.exception.BusinessException;
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

    public static final String TYPE_USER = "user";
    public static final String TYPE_ADMIN = "admin";

    private final TokenProperties tokenProperties;

    public TokenUtil(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    public String createToken(Long userId, String username) {
        return createToken(userId, username, TYPE_USER);
    }

    public String createAdminToken(Long adminId, String username) {
        return createToken(adminId, username, TYPE_ADMIN);
    }

    public Long parseUserId(String token) {
        Claims claims = parseClaims(token);
        String type = claims.get("type", String.class);
        if (type != null && !TYPE_USER.equals(type)) {
            throw new BusinessException("token 身份类型错误");
        }
        return parseSubjectId(claims);
    }

    public Long parseAdminId(String token) {
        Claims claims = parseClaims(token);
        String type = claims.get("type", String.class);
        if (!TYPE_ADMIN.equals(type)) {
            throw new BusinessException("管理员 token 无效");
        }
        return parseSubjectId(claims);
    }

    public String parseUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    private String createToken(Long subjectId, String username, String type) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(tokenProperties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(subjectId))
                .claim("username", username)
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(getKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new BusinessException("token 无效或已过期");
        }
    }

    private Long parseSubjectId(Claims claims) {
        try {
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