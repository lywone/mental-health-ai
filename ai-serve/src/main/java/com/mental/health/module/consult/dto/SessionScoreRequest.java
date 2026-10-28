package com.mental.health.module.consult.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会话评分请求：{score: 1-10}
 */
@Data
public class SessionScoreRequest {

    @NotNull(message = "评分不能为空")
    private Integer score;
}
