package com.mental.health.common;

import lombok.Getter;

/**
 * 错误码枚举（与接口设计文档 2.5 完全一致）
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),
    LOGIN_FAILED(1001, "账号或密码错误"),
    UNAUTHORIZED(1002, "未登录或 token 无效"),
    TOKEN_EXPIRED(1003, "登录已过期"),
    FORBIDDEN(1004, "无权限访问"),
    PARAM_ERROR(1101, "参数校验失败"),
    NOT_FOUND(1102, "数据不存在"),
    SESSION_NOT_FOUND(1201, "会话不存在或无权限"),
    MESSAGE_TOO_LONG(1202, "消息内容超长"),
    INVALID_MOOD_TAG(1203, "情绪标签非法"),
    SCORE_OUT_OF_RANGE(1204, "评分超出范围"),
    FILE_TYPE_NOT_SUPPORTED(1301, "文件类型不支持"),
    FILE_TOO_LARGE(1302, "文件超出大小限制"),
    SMS_TOO_FREQUENT(1401, "验证码发送过于频繁，请稍后再试"),
    SMS_CODE_INVALID(1402, "验证码错误或已过期"),
    SMS_SEND_LIMIT(1403, "今日验证码发送次数已达上限"),
    PHONE_ALREADY_REGISTERED(1404, "该手机号已注册，请直接登录"),
    AI_UNAVAILABLE(1501, "AI 服务暂时不可用"),
    INTERNAL_ERROR(500, "系统内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
