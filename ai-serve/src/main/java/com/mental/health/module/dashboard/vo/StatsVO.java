package com.mental.health.module.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计卡片 VO
 */
@Data
public class StatsVO {

    private long totalUsers;
    private long activeUsers;
    private long totalMoods;
    private long moodsToday;
    private long totalConsults;
    private long consultsToday;

    /** 全部情绪记录平均分（1 位小数），无记录为 null */
    private BigDecimal avgScore;

    /** 已结束会话时长均值（分钟，1 位小数），无记录为 null */
    private BigDecimal avgDurationMin;
}
