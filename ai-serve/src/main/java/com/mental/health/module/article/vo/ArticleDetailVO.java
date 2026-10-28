package com.mental.health.module.article.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章详情 VO（列表字段 + 正文全文）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailVO extends ArticleVO {

    private String content;
}
