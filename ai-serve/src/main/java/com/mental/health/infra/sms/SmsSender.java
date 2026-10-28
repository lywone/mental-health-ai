package com.mental.health.infra.sms;

/**
 * 短信发送通道抽象（当前实现 log，可扩展 aliyun/tencent）
 */
public interface SmsSender {

    /** 发送验证码短信 */
    void send(String phone, String code, String scene);
}
