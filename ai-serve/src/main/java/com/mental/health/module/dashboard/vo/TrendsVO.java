package com.mental.health.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 三条趋势序列（等长升序，date=MM-dd）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendsVO {

    private List<TrendPointVO> emotionTrend;
    private List<ConsultTrendPointVO> consultTrend;
    private List<ActivityTrendPointVO> activityTrend;
}
