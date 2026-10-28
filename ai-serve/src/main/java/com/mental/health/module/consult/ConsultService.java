package com.mental.health.module.consult;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.common.PageResult;
import com.mental.health.module.consult.vo.ChatMessageVO;
import com.mental.health.module.consult.vo.ConsultSessionDetailVO;
import com.mental.health.module.consult.vo.ConsultSessionVO;
import com.mental.health.module.consult.vo.UserSessionItemVO;
import com.mental.health.module.user.User;
import com.mental.health.module.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 咨询服务：管理端查看 + 用户端会话历史/结束/评分 + 聊天消息事务写库
 */
@Service
@RequiredArgsConstructor
public class ConsultService {

    /** 用户端会话历史返回条数上限 */
    private static final int MY_SESSION_LIMIT = 50;
    /** title 截断长度（与前端 firstUserText 同口径） */
    private static final int TITLE_MAX_LEN = 18;

    private final ConsultSessionMapper sessionMapper;
    private final ConsultMessageMapper messageMapper;
    private final UserMapper userMapper;

    // ---------- 管理端 ----------

    /** 分页列表：user 支持昵称或用户 ID 匹配，按 start_time 倒序 */
    public PageResult<ConsultSessionVO> pageForAdmin(String user, LocalDate startDate, LocalDate endDate,
                                                     String status, long page, long pageSize) {
        LambdaQueryWrapper<ConsultSession> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(user)) {
            wrapper.in(ConsultSession::getUserId, matchUserIds(user.trim()));
        }
        if (startDate != null) {
            wrapper.ge(ConsultSession::getStartTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.lt(ConsultSession::getStartTime, endDate.plusDays(1).atStartOfDay());
        }
        SessionStatus statusEnum = SessionStatus.ofLabel(status);
        if (statusEnum != null) {
            wrapper.eq(ConsultSession::getStatus, statusEnum.getCode());
        }
        wrapper.orderByDesc(ConsultSession::getStartTime);
        Page<ConsultSession> result = sessionMapper.selectPage(Page.of(page, pageSize), wrapper);
        Map<Long, String> nicknames = loadNicknames(result.getRecords().stream()
                .map(ConsultSession::getUserId).distinct().toList());
        return PageResult.of(result.convert(s -> toVO(s, nicknames.get(s.getUserId()))));
    }

    /** 会话详情（含消息，按时间升序）；调用方负责审计日志 */
    public ConsultSessionDetailVO detailForAdmin(Long id) {
        ConsultSession session = sessionMapper.selectById(id);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        ConsultSessionDetailVO vo = new ConsultSessionDetailVO();
        copyVO(session, vo, loadNicknames(List.of(session.getUserId())).get(session.getUserId()));
        vo.setMessages(listMessages(id).stream().map(this::toMessageVO).toList());
        return vo;
    }

    // ---------- 用户端 ----------

    /** 我的会话历史：仅本人，start_time 倒序，默认最近 50 条 */
    public List<UserSessionItemVO> listMine(Long uid) {
        List<ConsultSession> sessions = sessionMapper.selectList(new LambdaQueryWrapper<ConsultSession>()
                .eq(ConsultSession::getUserId, uid)
                .orderByDesc(ConsultSession::getStartTime)
                .last("LIMIT " + MY_SESSION_LIMIT));
        if (sessions.isEmpty()) {
            return List.of();
        }
        Map<Long, String> firstUserTexts = firstUserTexts(sessions.stream()
                .map(ConsultSession::getId).toList());
        return sessions.stream().map(s -> {
            UserSessionItemVO vo = new UserSessionItemVO();
            vo.setId(s.getId());
            String text = firstUserTexts.getOrDefault(s.getId(), "");
            vo.setTitle(text.isEmpty() ? "新会话" : text.substring(0, Math.min(TITLE_MAX_LEN, text.length())));
            vo.setStartTime(s.getStartTime());
            vo.setMessageCount(s.getMessageCount());
            SessionStatus st = SessionStatus.ofCode(s.getStatus());
            vo.setStatus(st == null ? "" : st.getLabel());
            return vo;
        }).toList();
    }

    /** 我的会话详情（含消息）：仅本人可查 */
    public ConsultSessionDetailVO detailMine(Long uid, Long sessionId) {
        ConsultSession session = requireOwned(uid, sessionId);
        ConsultSessionDetailVO vo = new ConsultSessionDetailVO();
        copyVO(session, vo, loadNicknames(List.of(uid)).get(uid));
        vo.setMessages(listMessages(sessionId).stream().map(this::toMessageVO).toList());
        return vo;
    }

    /** 结束会话（幂等）：status → 已结束，end_time=now，duration_min = max(1, 四舍五入分钟) */
    @Transactional
    public ConsultSessionVO endSession(Long uid, Long sessionId) {
        ConsultSession session = requireOwned(uid, sessionId);
        if (SessionStatus.ENDED.getCode() == session.getStatus()) {
            return toVO(session, loadNicknames(List.of(uid)).get(uid));
        }
        session.setStatus(SessionStatus.ENDED.getCode());
        session.setEndTime(LocalDateTime.now());
        long minutes = Math.max(1, Math.round(
                Duration.between(session.getStartTime(), session.getEndTime()).toMillis() / 60000.0));
        session.setDurationMin((int) minutes);
        sessionMapper.updateById(session);
        return toVO(session, loadNicknames(List.of(uid)).get(uid));
    }

    /** 会话评分：仅已结束会话，1-10（越界 1204） */
    @Transactional
    public ConsultSessionVO score(Long uid, Long sessionId, Integer score) {
        if (score == null || score < 1 || score > 10) {
            throw new BusinessException(ErrorCode.SCORE_OUT_OF_RANGE);
        }
        ConsultSession session = requireOwned(uid, sessionId);
        if (SessionStatus.ENDED.getCode() != session.getStatus()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅已结束的会话可以评分");
        }
        session.setScore(score);
        sessionMapper.updateById(session);
        return toVO(session, loadNicknames(List.of(uid)).get(uid));
    }

    // ---------- 聊天写库（事务） ----------

    /**
     * 新建/校验会话并保存用户消息（事务内），返回落库后的用户消息（含 createdAt）：
     * sessionId 为空 → 新建会话（进行中）；否则校验归属与未结束状态。
     */
    @Transactional
    public ConsultMessage saveUserMessage(Long uid, Long sessionId, String content) {
        Long sid;
        if (sessionId == null) {
            ConsultSession session = new ConsultSession();
            session.setUserId(uid);
            session.setStatus(SessionStatus.IN_PROGRESS.getCode());
            session.setStartTime(LocalDateTime.now());
            session.setMessageCount(0);
            session.setRiskFlag(0);
            sessionMapper.insert(session);
            sid = session.getId();
        } else {
            ConsultSession session = requireOwned(uid, sessionId);
            if (SessionStatus.IN_PROGRESS.getCode() != session.getStatus()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "会话已结束，无法继续发送消息");
            }
            sid = session.getId();
        }
        ConsultMessage message = new ConsultMessage();
        message.setSessionId(sid);
        message.setFromType(MessageFrom.USER.getCode());
        message.setContent(content);
        messageMapper.insert(message);
        sessionMapper.incrementMessageCount(sid);
        return message;
    }

    /** 保存 AI 回复（事务内），命中危机词时打 risk_flag=1 */
    @Transactional
    public ConsultMessage saveAiReply(Long sessionId, String content, boolean riskHit) {
        ConsultMessage message = new ConsultMessage();
        message.setSessionId(sessionId);
        message.setFromType(MessageFrom.AI.getCode());
        message.setContent(content);
        messageMapper.insert(message);
        sessionMapper.incrementMessageCount(sessionId);
        if (riskHit) {
            sessionMapper.update(null, new LambdaUpdateWrapper<ConsultSession>()
                    .eq(ConsultSession::getId, sessionId)
                    .set(ConsultSession::getRiskFlag, 1));
        }
        return message;
    }

    /** 最近 limit 条消息（倒序，最新在前，供 LLM 上下文） */
    public List<ConsultMessage> listRecent(Long sessionId, int limit) {
        return messageMapper.selectList(new LambdaQueryWrapper<ConsultMessage>()
                .eq(ConsultMessage::getSessionId, sessionId)
                .orderByDesc(ConsultMessage::getCreatedAt)
                .orderByDesc(ConsultMessage::getId)
                .last("LIMIT " + limit));
    }

    // ---------- 私有工具 ----------

    private ConsultSession requireOwned(Long uid, Long sessionId) {
        ConsultSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(uid)) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
        return session;
    }

    /** 会话全部消息（升序） */
    private List<ConsultMessage> listMessages(Long sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<ConsultMessage>()
                .eq(ConsultMessage::getSessionId, sessionId)
                .orderByAsc(ConsultMessage::getCreatedAt)
                .orderByAsc(ConsultMessage::getId));
    }

    /** user 参数匹配：昵称模糊 or 用户 ID（纯数字时额外精确匹配 id） */
    private List<Long> matchUserIds(String keyword) {
        List<Long> ids = new ArrayList<>();
        if (keyword.matches("\\d+")) {
            ids.add(Long.parseLong(keyword));
        }
        userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getNickname, keyword))
                .forEach(u -> ids.add(u.getId()));
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    /** 批量取用户昵称 */
    private Map<Long, String> loadNicknames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
    }

    /** 批量取每个会话的首条用户消息文本（时间升序取第一条） */
    private Map<Long, String> firstUserTexts(List<Long> sessionIds) {
        Map<Long, String> result = new LinkedHashMap<>();
        messageMapper.selectList(new LambdaQueryWrapper<ConsultMessage>()
                        .in(ConsultMessage::getSessionId, sessionIds)
                        .eq(ConsultMessage::getFromType, MessageFrom.USER.getCode())
                        .orderByAsc(ConsultMessage::getCreatedAt)
                        .orderByAsc(ConsultMessage::getId))
                .forEach(m -> result.putIfAbsent(m.getSessionId(), m.getContent() == null ? "" : m.getContent()));
        return result;
    }

    private ConsultSessionVO toVO(ConsultSession session, String nickname) {
        ConsultSessionVO vo = new ConsultSessionVO();
        copyVO(session, vo, nickname);
        return vo;
    }

    private void copyVO(ConsultSession session, ConsultSessionVO vo, String nickname) {
        vo.setId(session.getId());
        vo.setUserId(session.getUserId());
        vo.setUserNickname(nickname);
        vo.setStartTime(session.getStartTime());
        vo.setEndTime(session.getEndTime());
        vo.setDurationMin(session.getDurationMin());
        vo.setMessageCount(session.getMessageCount());
        vo.setScore(session.getScore());
        SessionStatus st = SessionStatus.ofCode(session.getStatus());
        vo.setStatus(st == null ? "" : st.getLabel());
    }

    private ChatMessageVO toMessageVO(ConsultMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        MessageFrom from = MessageFrom.ofCode(message.getFromType());
        vo.setFrom(from == null ? "user" : from.getLabel());
        vo.setTime(message.getCreatedAt());
        vo.setText(message.getContent());
        return vo;
    }
}
