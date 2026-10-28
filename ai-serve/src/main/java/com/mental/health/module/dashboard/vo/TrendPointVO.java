package com.mental.health.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 情绪趋势点位：{date, avgScore(当日无记录为 null), count}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointVO {

    private String date;

    private BigDecimal avgScore;

    private long count;
}
