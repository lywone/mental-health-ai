package com.mental.health.infra.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地存储配置项（application.yml storage.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /** 上传目录（默认 ./upload） */
    private String uploadDir = "./upload";

    /** 对外 URL 前缀（默认 /upload，需与静态映射一致） */
    private String urlPrefix = "/upload";
}
