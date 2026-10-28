package com.mental.health.infra.ai;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.module.consult.ConsultMessage;
import com.mental.health.module.consult.MessageFrom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 对话服务：OpenAI 兼容 /chat/completions（Spring RestClient，超时可配），失败直接抛出由调用方降级
 */
@Slf4j
@Service
public class AiChatService {

    /** 轮换兜底语料的游标（跨请求顺序轮换，与 Mock 行为一致） */
    private final AtomicInteger fallbackIdx = new AtomicInteger();

    private final LlmProperties llmProperties;

    private volatile RestClient restClient;

    public AiChatService(LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    /** 对外消息结构（OpenAI 兼容） */
    public record ChatMessage(String role, String content) {
    }

    private record ChatRequest(String model, List<ChatMessage> messages, boolean stream) {
    }

    private record ChatResponse(List<Choice> choices) {
    }

    private record Choice(ChatMessage message) {
    }

    /**
     * 调用 LLM：system prompt + 最近历史消息 + 当前输入。
     * 未启用 / 超时 / 异常 / 响应非法 → 抛 BusinessException(1501)，由 ChatService 降级到本地语料。
     */
    public String chat(List<ChatMessage> history, String userInput) {
        if (!llmProperties.isEnabled()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE, "LLM 未启用");
        }
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", llmProperties.getSystemPrompt()));
        messages.addAll(history);
        messages.add(new ChatMessage("user", userInput));

        try {
            ChatResponse response = client().post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + llmProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ChatRequest(llmProperties.getModel(), messages, false))
                    .retrieve()
                    .body(ChatResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()
                    || response.choices().get(0).message() == null
                    || response.choices().get(0).message().content() == null
                    || response.choices().get(0).message().content().isBlank()) {
                throw new BusinessException(ErrorCode.AI_UNAVAILABLE, "LLM 返回为空");
            }
            return response.choices().get(0).message().content();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("LLM 调用失败，进入降级: {}", e.getMessage());
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE, "LLM 调用失败");
        }
    }

    /** 降级回复：先按关键词规则命中，否则兜底语料顺序轮换 */
    public String fallbackReply(String userText) {
        String rule = FallbackReplies.matchRule(userText);
        if (rule != null) {
            return rule;
        }
        int idx = Math.floorMod(fallbackIdx.getAndIncrement(), FallbackReplies.FALLBACK_REPLIES.size());
        return FallbackReplies.FALLBACK_REPLIES.get(idx);
    }

    /** 历史消息 → LLM 角色（用户 → user，AI → assistant），仅保留最近 limit 条（升序） */
    public List<ChatMessage> toLlmHistory(List<ConsultMessage> recentDesc, int limit) {
        List<ConsultMessage> history = recentDesc.stream()
                .skip(1)  // 跳过最新一条（即刚保存的当前输入）
                .limit(limit)
                .toList();
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            ConsultMessage m = history.get(i);
            String role = MessageFrom.USER.getCode() == m.getFromType() ? "user" : "assistant";
            messages.add(new ChatMessage(role, m.getContent()));
        }
        return messages;
    }

    private RestClient client() {
        if (restClient == null) {
            synchronized (this) {
                if (restClient == null) {
                    ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                            .withConnectTimeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
                            .withReadTimeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()));
                    restClient = RestClient.builder()
                            .baseUrl(llmProperties.getBaseUrl())
                            .requestFactory(ClientHttpRequestFactories.get(settings))
                            .build();
                }
            }
        }
        return restClient;
    }
}
