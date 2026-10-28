package com.mental.health.module.article.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端文章卡片 VO
 */
@Data
public class ArticleCardVO {

    private Long id;

    private String title;

    private String category;

    private String cover;

    private String summary;

    private Integer views;

    private LocalDateTime publishTime;
}
