package com.mental.health.module.mood;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;

import java.util.List;

/**
 * 情绪标签白名单（设计文档 4.1，至多 2 个）
 */
public final class MoodTags {

    public static final List<String> ALL = List.of("开心", "平静", "焦虑", "低落", "愤怒", "疲惫");

    private MoodTags() {
    }

    /** 校验：≤2 个且全部在白名单内，违规抛 1203 */
    public static void check(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        if (tags.size() > 2) {
            throw new BusinessException(ErrorCode.INVALID_MOOD_TAG, "情绪标签最多 2 个");
        }
        for (String tag : tags) {
            if (tag == null || !ALL.contains(tag)) {
                throw new BusinessException(ErrorCode.INVALID_MOOD_TAG, "情绪标签非法，可选值：" + ALL);
            }
        }
    }
}
