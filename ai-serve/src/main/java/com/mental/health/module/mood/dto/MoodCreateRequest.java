package com.mental.health.module.mood.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 记录心情请求
 */
@Data
public class MoodCreateRequest {

    /** 1-10，越界返回 1204 */
    @NotNull(message = "情绪评分不能为空")
    private Integer score;

    /** ≤2 个且在白名单，违规返回 1203 */
    private List<String> tags;

    /** ≤200 字 */
    @Size(max = 200, message = "心情文字不能超过 200 字")
    private String content;
}
