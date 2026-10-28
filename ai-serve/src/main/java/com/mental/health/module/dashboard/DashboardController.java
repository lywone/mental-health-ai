package com.mental.health.module.dashboard;

import com.mental.health.common.Result;
import com.mental.health.module.dashboard.vo.StatsVO;
import com.mental.health.module.dashboard.vo.TrendsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据分析接口（管理后台）
 */
@Tag(name = "数据分析-管理端")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "统计卡片")
    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.ok(dashboardService.stats());
    }

    @Operation(summary = "三条趋势")
    @GetMapping("/trends")
    public Result<TrendsVO> trends(@RequestParam(required = false) Integer days) {
        return Result.ok(dashboardService.trends(days));
    }
}
