package com.mental.health.module.mood;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 情绪日志（t_mood_log），tags 逗号分隔存储（≤2 个，白名单见 4.1）
 */
@Data
@TableName("t_mood_log")
public class MoodLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 情绪评分 1-10 */
    private Integer score;

    /** 逗号分隔，最多 2 个 */
    private String tags;

    /** 心情文字，可为空，≤200 字 */
    private String content;

    /** 预留：来源咨询会话 */
    private Long sessionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
