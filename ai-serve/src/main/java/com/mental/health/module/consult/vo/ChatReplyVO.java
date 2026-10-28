package com.mental.health.module.consult.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息响应：{sessionId, userMessage, reply}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatReplyVO {

    private Long sessionId;

    private ChatMessageVO userMessage;

    private ChatMessageVO reply;
}
