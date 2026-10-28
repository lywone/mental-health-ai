package com.mental.health.module.consult.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送消息请求
 */
@Data
public class ChatSendRequest {

    /** 为空 → 新建会话；非空须属于当前用户且未结束 */
    private Long sessionId;

    /** 1-500 字；超长返回 1202 */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
