package com.mental.health.infra.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 日志通道：验证码仅打印到日志，不真实发送（默认实现，开发/演示用）
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.channel", havingValue = "log", matchIfMissing = true)
public class LogSmsSender implements SmsSender {

    @Override
    public void send(String phone, String code, String scene) {
        log.info("[SMS-LOG] 向 {} 发送验证码 {}（scene={}，仅日志通道，未真实发送）", phone, code, scene);
    }
}
