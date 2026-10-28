package com.mental.health.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 咨询趋势点位：{date, count, userCount}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultTrendPointVO {

    private String date;

    private long count;

    private long userCount;
}
