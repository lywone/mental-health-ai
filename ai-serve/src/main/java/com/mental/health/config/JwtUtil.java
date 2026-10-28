package com.mental.health.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具（jjwt 0.12 API）：签发与解析，claims 含 uid/role/nickname
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    private final JwtProperties jwtProperties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 签发管理员 token（2h） */
    public String generateAdminToken(Long adminId, String username, String nickname) {
        return generate(adminId, ROLE_ADMIN, nickname, jwtProperties.getAdminExpireSeconds());
    }

    /** 签发用户 token（24h） */
    public String generateUserToken(Long userId, String nickname) {
        return generate(userId, ROLE_USER, nickname, jwtProperties.getUserExpireSeconds());
    }

    private String generate(Long uid, String role, String nickname, long expireSeconds) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(uid))
                .claim("uid", uid)
                .claim("role", role)
                .claim("nickname", nickname)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key())
                .compact();
    }

    /** 解析 token，非法返回 null（过期抛 ExpiredJwtException 由调用方处理） */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    /** 是否已过期 */
    public boolean isExpired(String token) {
        try {
            parse(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 剩余有效秒数（用于登出黑名单 TTL），解析失败返回 0 */
    public long remainingSeconds(String token) {
        try {
            Claims claims = parse(token);
            long remain = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remain);
        } catch (ExpiredJwtException e) {
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
