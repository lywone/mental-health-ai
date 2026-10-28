package com.mental.health.infra.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mental.health.module.consult.ConsultMessage;
import com.mental.health.module.consult.ConsultSession;
import com.mental.health.module.consult.ConsultMessageMapper;
import com.mental.health.module.consult.ConsultSessionMapper;
import com.mental.health.module.consult.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话超时任务：每小时扫描「进行中且最后一条消息时间超过 24h」的会话并自动结束（与手动结束同一规则）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionTimeoutJob {

    /** 静默超时阈值 */
    private static final int SILENT_HOURS = 24;

    private final ConsultSessionMapper sessionMapper;
    private final ConsultMessageMapper messageMapper;

    /** 每小时执行一次（启动后延迟 1 分钟） */
    @Scheduled(fixedDelay = 3600_000L, initialDelay = 60_000L)
    public void closeTimeoutSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(SILENT_HOURS);
        List<ConsultSession> sessions = sessionMapper.selectList(new LambdaQueryWrapper<ConsultSession>()
                .eq(ConsultSession::getStatus, SessionStatus.IN_PROGRESS.getCode()));
        int closed = 0;
        for (ConsultSession session : sessions) {
            LocalDateTime lastActive = lastMessageTime(session.getId());
            if (lastActive == null) {
                lastActive = session.getStartTime();
            }
            if (lastActive != null && lastActive.isBefore(threshold)) {
                endSession(session);
                closed++;
            }
        }
        if (closed > 0) {
            log.info("会话超时任务：自动结束 {} 个静默超过 {} 小时的会话", closed, SILENT_HOURS);
        }
    }

    private LocalDateTime lastMessageTime(Long sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<ConsultMessage>()
                        .eq(ConsultMessage::getSessionId, sessionId)
                        .orderByDesc(ConsultMessage::getCreatedAt)
                        .orderByDesc(ConsultMessage::getId)
                        .last("LIMIT 1"))
                .stream().findFirst()
                .map(ConsultMessage::getCreatedAt)
                .orElse(null);
    }

    /** 与用户主动结束同一规则：end_time=now，duration_min=max(1, 四舍五入分钟) */
    private void endSession(ConsultSession session) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = Math.max(1, Math.round(
                Duration.between(session.getStartTime(), now).toMillis() / 60000.0));
        sessionMapper.update(null, new LambdaUpdateWrapper<ConsultSession>()
                .eq(ConsultSession::getId, session.getId())
                .eq(ConsultSession::getStatus, SessionStatus.IN_PROGRESS.getCode())
                .set(ConsultSession::getStatus, SessionStatus.ENDED.getCode())
                .set(ConsultSession::getEndTime, now)
                .set(ConsultSession::getDurationMin, (int) minutes));
    }
}
