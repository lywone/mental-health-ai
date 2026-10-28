package com.mental.health.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项（application.yml jwt.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 签名密钥（至少 32 字节） */
    private String secret;

    /** 管理端 token 有效期（秒），默认 2h */
    private long adminExpireSeconds = 7200;

    /** 用户端 token 有效期（秒），默认 24h */
    private long userExpireSeconds = 86400;
}
