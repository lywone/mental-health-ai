package com.mental.health.module.consult;

import lombok.Getter;

import java.util.Arrays;

/**
 * 消息来源：1 用户（VO 输出 "user"）/ 2 AI（VO 输出 "ai"）
 */
@Getter
public enum MessageFrom {

    USER(1, "user"),
    AI(2, "ai");

    private final int code;
    private final String label;

    MessageFrom(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MessageFrom ofCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(f -> f.code == code).findFirst().orElse(null);
    }
}
