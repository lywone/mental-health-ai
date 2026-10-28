package com.mental.health.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mental.health.common.ErrorCode;
import com.mental.health.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * 安全配置：无状态 JWT + 双角色隔离 + 统一 401/403 JSON 输出
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口：登录 / 用户端注册与验证码 / API 文档 / 上传静态资源
                        .requestMatchers("POST", "/api/auth/admin/login").permitAll()
                        .requestMatchers("POST", "/api/auth/user/dev-login").permitAll()
                        .requestMatchers("POST", "/api/auth/user/sms/send").permitAll()
                        .requestMatchers("POST", "/api/auth/user/register").permitAll()
                        .requestMatchers("POST", "/api/auth/user/login").permitAll()
                        // /api/auth 下其余接口按路径段隔离角色（logout/me → ADMIN）
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/user/**").hasRole("USER")
                        .requestMatchers("/doc.html", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/webjars/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        // 角色隔离
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .requestMatchers("/api/common/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, e) -> {
                            // token 过期/黑名单 → 1003，其余未认证 → 1002
                            String expired = (String) request.getAttribute(JwtAuthFilter.AUTH_EXPIRED_ATTR);
                            ErrorCode code = expired != null ? ErrorCode.TOKEN_EXPIRED : ErrorCode.UNAUTHORIZED;
                            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, code);
                        })
                        .accessDeniedHandler((request, response, e) ->
                                writeJson(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeJson(HttpServletResponse response, int httpStatus, ErrorCode code) {
        try {
            response.setStatus(httpStatus);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(code)));
        } catch (Exception e) {
            log.warn("输出认证/授权错误 JSON 失败", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
