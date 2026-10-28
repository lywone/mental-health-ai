package com.mental.health.module.consult.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端会话历史条目：{id, title, startTime, messageCount, status}
 */
@Data
public class UserSessionItemVO {

    private Long id;

    /** 会话内首条用户消息截断 18 字 */
    private String title;

    private LocalDateTime startTime;

    private Integer messageCount;

    private String status;
}
