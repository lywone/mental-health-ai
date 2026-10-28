package com.mental.health.module.consult.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询会话 VO（列表，不含消息体）
 */
@Data
public class ConsultSessionVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationMin;

    private Integer messageCount;

    /** 进行中恒为 null */
    private Integer score;

    /** 中文枚举：进行中 / 已结束 */
    private String status;
}
