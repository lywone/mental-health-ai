package com.mental.health.module.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 按日情绪聚合原始行（Mapper 查询结果）
 */
@Data
public class DailyMoodAgg {

    private String day;

    private BigDecimal avgScore;

    private Long cnt;
}
