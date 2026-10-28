package com.mental.health.module.consult;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询消息（t_consult_message）
 */
@Data
@TableName("t_consult_message")
public class ConsultMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    /** 1 用户 / 2 AI */
    private Integer fromType;

    /** 用户输入 ≤500，AI 回复可能较长 */
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
