package com.mental.health.module.dashboard;

import com.mental.health.module.dashboard.vo.ActivityTrendPointVO;
import com.mental.health.module.dashboard.vo.ConsultTrendPointVO;
import com.mental.health.module.dashboard.vo.DailyActivityAgg;
import com.mental.health.module.dashboard.vo.DailyConsultAgg;
import com.mental.health.module.dashboard.vo.DailyMoodAgg;
import com.mental.health.module.dashboard.vo.StatsVO;
import com.mental.health.module.dashboard.vo.TrendPointVO;
import com.mental.health.module.dashboard.vo.TrendsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据分析服务：统计卡片与三条趋势（口径与 Mock 严格对齐）
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");

    private final DashboardMapper dashboardMapper;

    /** 统计卡片 */
    public StatsVO stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();

        StatsVO vo = new StatsVO();
        vo.setTotalUsers(dashboardMapper.countUsers());
        vo.setActiveUsers(dashboardMapper.countActiveUsers(sevenDaysAgo));
        vo.setTotalMoods(dashboardMapper.countMoods());
        vo.setMoodsToday(dashboardMapper.countMoodsSince(todayStart));
        vo.setTotalConsults(dashboardMapper.countConsults());
        vo.setConsultsToday(dashboardMapper.countConsultsSince(todayStart));
        vo.setAvgScore(dashboardMapper.avgScore());
        vo.setAvgDurationMin(dashboardMapper.avgDurationMin());
        return vo;
    }

    /** 三条趋势：days 默认 30、最大 90，序列等长升序，date=MM-dd */
    public TrendsVO trends(Integer days) {
        int n = days == null ? 30 : Math.min(90, Math.max(1, days));
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(n - 1L);

        // 当日序列模板（升序）
        List<LocalDate> range = start.datesUntil(today.plusDays(1)).toList();
        Map<String, String> dateLabels = range.stream()
                .collect(Collectors.toMap(DAY_KEY::format, d -> d.format(MM_DD),
                        (a, b) -> a, java.util.LinkedHashMap::new));

        LocalDateTime since = start.atStartOfDay();

        Map<String, DailyMoodAgg> moodMap = toMap(dashboardMapper.moodDailyAgg(since), DailyMoodAgg::getDay);
        Map<String, DailyConsultAgg> consultMap = toMap(dashboardMapper.consultDailyAgg(since), DailyConsultAgg::getDay);
        Map<String, DailyActivityAgg> activityMap = toMap(dashboardMapper.activityDailyAgg(since), DailyActivityAgg::getDay);

        List<TrendPointVO> emotionTrend = dateLabels.entrySet().stream()
                .map(e -> {
                    DailyMoodAgg agg = moodMap.get(e.getKey());
                    return new TrendPointVO(e.getValue(), agg == null ? null : agg.getAvgScore(),
                            agg == null ? 0 : agg.getCnt());
                })
                .toList();

        List<ConsultTrendPointVO> consultTrend = dateLabels.entrySet().stream()
                .map(e -> {
                    DailyConsultAgg agg = consultMap.get(e.getKey());
                    return new ConsultTrendPointVO(e.getValue(),
                            agg == null ? 0 : agg.getCnt(), agg == null ? 0 : agg.getUserCnt());
                })
                .toList();

        List<ActivityTrendPointVO> activityTrend = dateLabels.entrySet().stream()
                .map(e -> {
                    DailyActivityAgg agg = activityMap.get(e.getKey());
                    return new ActivityTrendPointVO(e.getValue(), agg == null ? 0 : agg.getActive());
                })
                .toList();

        return new TrendsVO(emotionTrend, consultTrend, activityTrend);
    }

    private <T> Map<String, T> toMap(List<T> list, Function<T, String> key) {
        return list.stream().collect(Collectors.toMap(key, item -> item, (a, b) -> a));
    }
}
