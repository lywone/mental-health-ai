package com.mental.health.module.mood.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 情绪日志 VO
 */
@Data
public class MoodLogVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private LocalDateTime time;

    private Integer score;

    /** 数组，≤2 个 */
    private List<String> tags;

    /** 可为 "" */
    private String content;
}
