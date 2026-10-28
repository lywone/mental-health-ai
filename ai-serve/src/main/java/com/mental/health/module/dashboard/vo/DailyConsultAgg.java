package com.mental.health.module.dashboard.vo;

import lombok.Data;

/**
 * 按日咨询聚合原始行（Mapper 查询结果）
 */
@Data
public class DailyConsultAgg {

    private String day;

    private Long cnt;

    private Long userCnt;
}
