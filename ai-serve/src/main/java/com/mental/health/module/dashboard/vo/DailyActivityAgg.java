package com.mental.health.module.dashboard.vo;

import lombok.Data;

/**
 * 按日活跃用户聚合原始行（Mapper 查询结果）
 */
@Data
public class DailyActivityAgg {

    private String day;

    private Long active;
}
