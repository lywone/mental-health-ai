package com.mental.health.module.article.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章 VO（管理端列表，不含正文）
 */
@Data
public class ArticleVO {

    private Long id;

    private String title;

    private String category;

    /** 封面 URL，可为 "" */
    private String cover;

    private String summary;

    /** 中文枚举：草稿 / 已发布 / 已下架 */
    private String status;

    private Integer views;

    private LocalDateTime publishTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
