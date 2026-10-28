package com.mental.health.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证相关开关（application.yml auth.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /** 用户端开发登录开关（生产必须关闭，dev 环境覆盖为 true） */
    private boolean devLoginEnabled = false;
}
