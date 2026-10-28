package com.mental.health.infra.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信验证码配置（application.yml sms.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /** 发送通道：log / aliyun / tencent */
    private String channel = "log";

    /** 验证码有效期（秒） */
    private int codeTtlSeconds = 300;

    /** 同一手机号发送间隔（秒） */
    private int sendIntervalSeconds = 60;

    /** 同一手机号每日发送上限 */
    private int dailyLimit = 10;

    /** 接口回显验证码（仅开发联调用，生产必须 false） */
    private boolean echoDevCode = false;
}
