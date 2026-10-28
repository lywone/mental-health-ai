package com.mental.health.module.auth.dto;

/**
 * 发送验证码响应（devCode 仅在 log 通道 + 联调开关开启时返回）
 */
public record SmsSendResponse(int intervalSeconds, String devCode) {
}
