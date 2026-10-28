package com.mental.health.module.consult;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.infra.ai.AiChatService;
import com.mental.health.infra.ai.CrisisWordDetector;
import com.mental.health.module.consult.dto.ChatSendRequest;
import com.mental.health.module.consult.vo.ChatMessageVO;
import com.mental.health.module.consult.vo.ChatReplyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 聊天编排：限流 → 事务写用户消息 → 危机检测/LLM（事务外调用，异常降级）→ 事务写 AI 回复
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    /** 限流：每用户 10 次/分钟（Redis INCR） */
    private static final int RATE_LIMIT = 10;
    private static final String RATE_LIMIT_PREFIX = "chat:rl:";
    private static final DateTimeFormatter MINUTE_KEY = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final ConsultService consultService;
    private final AiChatService aiChatService;
    private final CrisisWordDetector crisisWordDetector;
    private final StringRedisTemplate stringRedisTemplate;

    public ChatReplyVO sendMessage(Long uid, ChatSendRequest request) {
        checkRateLimit(uid);

        String content = request.getContent();
        if (content != null && content.length() > 500) {
            throw new BusinessException(ErrorCode.MESSAGE_TOO_LONG);
        }

        // 1. 事务内：新建/校验会话 + 保存用户消息（返回落库消息，含 createdAt）
        ConsultMessage userMessage = consultService.saveUserMessage(uid, request.getSessionId(), content);
        Long sessionId = userMessage.getSessionId();

        // 2. AI 回复（事务外调用）：危机词命中 → 固定干预话术；否则 LLM → 降级语料
        boolean riskHit = crisisWordDetector.hit(content);
        String replyText;
        if (riskHit) {
            log.warn("[危机干预] 用户 uid={} 命中危机词，会话 id={} 已标记 risk_flag=1", uid, sessionId);
            replyText = CrisisWordDetector.CRISIS_REPLY;
        } else {
            try {
                replyText = aiChatService.chat(
                        aiChatService.toLlmHistory(consultService.listRecent(sessionId, 11), 10), content);
            } catch (Exception e) {
                log.warn("AI 不可用，走降级语料: {}", e.getMessage());
                replyText = aiChatService.fallbackReply(content);
            }
        }

        // 3. 事务内：保存 AI 回复（risk_flag=1 时一并更新）
        ConsultMessage aiMessage = consultService.saveAiReply(sessionId, replyText, riskHit);

        // 4. 组装响应（时间为落库时间）
        ChatMessageVO userMessageVO = new ChatMessageVO();
        userMessageVO.setFrom(MessageFrom.USER.getLabel());
        userMessageVO.setTime(userMessage.getCreatedAt());
        userMessageVO.setText(content);
        ChatMessageVO replyVO = new ChatMessageVO();
        replyVO.setFrom(MessageFrom.AI.getLabel());
        replyVO.setTime(aiMessage.getCreatedAt());
        replyVO.setText(replyText);
        return new ChatReplyVO(sessionId, userMessageVO, replyVO);
    }

    private void checkRateLimit(Long uid) {
        String key = RATE_LIMIT_PREFIX + uid + ":" + LocalDateTime.now().format(MINUTE_KEY);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(65));
        }
        if (count != null && count > RATE_LIMIT) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "发送太频繁，请稍后再试");
        }
    }
}
