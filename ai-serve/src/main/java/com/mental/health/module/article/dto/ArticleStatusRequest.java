package com.mental.health.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发布/下架请求：action = publish | offline
 */
@Data
public class ArticleStatusRequest {

    @NotBlank(message = "action 不能为空")
    private String action;
}
