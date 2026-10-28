package com.mental.health.module.article;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

/**
 * 文章状态：DB 存 TINYINT，接口层使用中文字符串
 */
@Getter
public enum ArticleStatus {

    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    OFFLINE(3, "已下架");

    private final int code;
    private final String label;

    ArticleStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ArticleStatus ofCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.code == code).findFirst().orElse(null);
    }

    /** 中文 → 枚举；label 为空返回 null，非法抛 1101 */
    public static ArticleStatus ofLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.label.equals(label)).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "文章状态非法：" + label));
    }
}
