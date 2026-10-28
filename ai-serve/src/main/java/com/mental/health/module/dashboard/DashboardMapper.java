package com.mental.health.module.dashboard;

import com.mental.health.module.dashboard.vo.DailyActivityAgg;
import com.mental.health.module.dashboard.vo.DailyConsultAgg;
import com.mental.health.module.dashboard.vo.DailyMoodAgg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 数据分析统计 Mapper（原生 @Select，口径见设计文档 6.3）
 */
@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM t_user")
    long countUsers();

    /** 近 N 天有情绪记录或咨询会话的去重用户数 */
    @Select("""
            SELECT COUNT(DISTINCT t.uid) FROM (
                SELECT user_id AS uid FROM t_mood_log WHERE created_at >= #{since}
                UNION ALL
                SELECT user_id FROM t_consult_session WHERE start_time >= #{since}
            ) t
            """)
    long countActiveUsers(@Param("since") LocalDateTime since);

    @Select("SELECT COUNT(*) FROM t_mood_log")
    long countMoods();

    @Select("SELECT COUNT(*) FROM t_mood_log WHERE created_at >= #{dayStart}")
    long countMoodsSince(@Param("dayStart") LocalDateTime dayStart);

    @Select("SELECT COUNT(*) FROM t_consult_session")
    long countConsults();

    @Select("SELECT COUNT(*) FROM t_consult_session WHERE start_time >= #{dayStart}")
    long countConsultsSince(@Param("dayStart") LocalDateTime dayStart);

    @Select("SELECT AVG(score) FROM t_mood_log")
    java.math.BigDecimal avgScore();

    @Select("SELECT AVG(duration_min) FROM t_consult_session WHERE status = 2")
    java.math.BigDecimal avgDurationMin();

    /** 按日情绪均分与条数（day = yyyy-MM-dd） */
    @Select("""
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS day,
                   ROUND(AVG(score), 1) AS avg_score,
                   COUNT(*) AS cnt
            FROM t_mood_log
            WHERE created_at >= #{since}
            GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
            """)
    java.util.List<DailyMoodAgg> moodDailyAgg(@Param("since") LocalDateTime since);

    /** 按日会话数与去重用户数 */
    @Select("""
            SELECT DATE_FORMAT(start_time, '%Y-%m-%d') AS day,
                   COUNT(*) AS cnt,
                   COUNT(DISTINCT user_id) AS user_cnt
            FROM t_consult_session
            WHERE start_time >= #{since}
            GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d')
            """)
    java.util.List<DailyConsultAgg> consultDailyAgg(@Param("since") LocalDateTime since);

    /** 按日活跃用户数（当日有情绪记录或会话的去重用户） */
    @Select("""
            SELECT t.day, COUNT(DISTINCT t.uid) AS active FROM (
                SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS day, user_id AS uid
                FROM t_mood_log WHERE created_at >= #{since}
                UNION ALL
                SELECT DATE_FORMAT(start_time, '%Y-%m-%d') AS day, user_id AS uid
                FROM t_consult_session WHERE start_time >= #{since}
            ) t
            GROUP BY t.day
            """)
    java.util.List<DailyActivityAgg> activityDailyAgg(@Param("since") LocalDateTime since);
}
