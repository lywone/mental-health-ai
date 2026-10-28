package com.mental.health.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活跃度趋势点位：{date, activeUsers}（当日有情绪记录或会话的去重用户数）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTrendPointVO {

    private String date;

    private long activeUsers;
}
