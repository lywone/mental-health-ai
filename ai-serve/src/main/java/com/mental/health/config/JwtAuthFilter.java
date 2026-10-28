package com.mental.health.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：解析 token → 构建 CurrentUser → 写入 SecurityContext；黑名单 token 直接忽略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String TOKEN_ATTR = "jwt_token";
    public static final String AUTH_EXPIRED_ATTR = "auth_expired";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // 登出黑名单：命中则视为未认证
                Boolean blacklisted = stringRedisTemplate.hasKey(BLACKLIST_PREFIX + token);
                if (Boolean.TRUE.equals(blacklisted)) {
                    request.setAttribute(AUTH_EXPIRED_ATTR, "blacklisted");
                } else {
                    Claims claims = jwtUtil.parse(token);
                    Long uid = toLong(claims.get("uid"));
                    String role = claims.get("role", String.class);
                    String nickname = claims.get("nickname", String.class);
                    if (uid != null && role != null) {
                        CurrentUser principal = new CurrentUser(uid, role, nickname);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        request.setAttribute(TOKEN_ATTR, token);
                    }
                }
            } catch (ExpiredJwtException e) {
                request.setAttribute(AUTH_EXPIRED_ATTR, "expired");
            } catch (Exception e) {
                log.debug("token 解析失败: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private Long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : null;
    }

    public static String blacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }
}
