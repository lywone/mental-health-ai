package com.mental.health.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文章新增/编辑请求（全量更新）
 */
@Data
public class ArticleSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题不能超过 50 字")
    private String title;

    @NotBlank(message = "分类不能为空")
    private String category;

    /** 封面 URL（先调上传接口） */
    private String cover;

    @Size(max = 100, message = "摘要不能超过 100 字")
    private String summary;

    @NotBlank(message = "正文不能为空")
    private String content;
}
