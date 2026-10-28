package com.mental.health.module.consult.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 会话详情 VO：会话字段 + 消息列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultSessionDetailVO extends ConsultSessionVO {

    private List<ChatMessageVO> messages;
}
