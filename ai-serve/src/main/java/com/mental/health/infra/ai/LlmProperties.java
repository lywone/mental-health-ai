package com.mental.health.infra.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 配置项（application.yml llm.*，OpenAI 兼容接口）
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /** 是否启用真实 LLM 调用；false 时直接走本地降级语料 */
    private boolean enabled = false;

    /** OpenAI 兼容 base-url，如 https://api.deepseek.com */
    private String baseUrl;

    private String apiKey;

    private String model;

    /** 超时秒数（默认 5s） */
    private int timeoutSeconds = 5;

    /** 人设 Prompt（助手名「宁澄AI助手」、自称「小暖」） */
    private String systemPrompt;
}
