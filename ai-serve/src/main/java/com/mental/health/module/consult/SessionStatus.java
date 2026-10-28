package com.mental.health.module.consult;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

/**
 * 会话状态：1 进行中 / 2 已结束
 */
@Getter
public enum SessionStatus {

    IN_PROGRESS(1, "进行中"),
    ENDED(2, "已结束");

    private final int code;
    private final String label;

    SessionStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SessionStatus ofCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.code == code).findFirst().orElse(null);
    }

    /** 中文 → 枚举；label 为空返回 null，非法抛 1101 */
    public static SessionStatus ofLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.label.equals(label)).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "会话状态非法：" + label));
    }
}
