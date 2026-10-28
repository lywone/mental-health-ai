package com.mental.health.module.consult;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询会话（t_consult_session）
 */
@Data
@TableName("t_consult_session")
public class ConsultSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 1 进行中 / 2 已结束 */
    private Integer status;

    /** 首条消息时间 */
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 时长（分钟，≥1），结束时计算 */
    private Integer durationMin;

    /** 冗余消息数，事务内维护 */
    private Integer messageCount;

    /** 会话评分 1-10，仅已结束可评 */
    private Integer score;

    /** 0/1 危机干预标记 */
    private Integer riskFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
