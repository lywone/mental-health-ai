package com.mental.health.module.article;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;

import java.util.List;

/**
 * 文章分类白名单（设计文档 4.1）
 */
public final class ArticleCategories {

    public static final List<String> ALL = List.of("心理科普", "情绪管理", "睡眠健康", "人际关系");

    private ArticleCategories() {
    }

    public static void check(String category) {
        if (category == null || !ALL.contains(category)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文章分类非法，可选值：" + ALL);
        }
    }
}
