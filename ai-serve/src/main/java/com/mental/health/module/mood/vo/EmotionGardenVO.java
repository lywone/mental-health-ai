package com.mental.health.module.mood.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 情绪花园 VO（今日最新一条；score≤3 低落 / 4-6 中性 / ≥7 不错）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionGardenVO {

    private Integer score;

    /** score × 10 */
    private Integer value;

    /** 低落 / 中性 / 不错 */
    private String state;

    /** 低落 #F56C6C / 中性 #F0A8B8 / 不错 #8FD3A8 */
    private String color;

    private String tip;

    private LocalDateTime time;
}
