package com.mental.health.module.consult.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询消息 VO：{from: "user"/"ai", time, text}
 */
@Data
public class ChatMessageVO {

    private String from;

    private LocalDateTime time;

    private String text;
}
