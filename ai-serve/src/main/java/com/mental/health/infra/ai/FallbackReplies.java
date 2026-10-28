package com.mental.health.infra.ai;

import java.util.List;

/**
 * 降级语料常量：与前端 Mock（src/mock/data.js）的 AI_REPLY_RULES / AI_FALLBACK_REPLIES 原样一致
 */
public final class FallbackReplies {

    /** 关键词规则（语料来源：data.js AI_REPLY_RULES） */
    public record Rule(List<String> keys, String text) {
    }

    public static final List<Rule> REPLY_RULES = List.of(
            new Rule(List.of("失眠", "睡不", "睡眠"),
                    "睡眠困扰确实很影响状态。可以试试睡前一小时远离手机，做几组缓慢的深呼吸。愿意说说这种情况大概持续多久了吗？"),
            new Rule(List.of("焦虑", "紧张", "慌"),
                    "焦虑是身体在提醒我们「在意」。试着给此刻的感受命名——「我现在感到焦虑」，这本身就能让情绪缓和下来。想聊聊是什么让你有这种感觉吗？"),
            new Rule(List.of("压力", "加班", "累"),
                    "听起来你最近承受了很多。先照顾好自己：规律三餐、每小时起来活动几分钟。压力很大的时候，随时可以来和我聊聊。"),
            new Rule(List.of("低落", "难过", "伤心", "沮丧"),
                    "谢谢你愿意说出来。低落的情绪不必急着赶走，给它一点空间，也给自己一点温柔。愿意和我多说说发生了什么吗？"),
            new Rule(List.of("开心", "高兴", "不错", "很好"),
                    "真为你高兴！把这份好心情记下来吧，情绪日志会帮你看见自己一点点变好的轨迹。今天有什么特别开心的事吗？"),
            new Rule(List.of("孤独", "一个人"),
                    "孤独的感觉并不容易，但此刻你愿意说出来，就是一次温暖的连接。我会一直在这里陪你。"),
            new Rule(List.of("愤怒", "生气", "吵架"),
                    "愤怒往往是委屈或边界被触碰的信号。先深呼吸让自己缓一缓，等平静一些，我们再来梳理发生了什么，好吗？"));

    /** 兜底文案（语料来源：data.js AI_FALLBACK_REPLIES，顺序轮换） */
    public static final List<String> FALLBACK_REPLIES = List.of(
            "我在认真听。愿意再多说一点吗？无论是心情还是经历，这里都是安全的空间。",
            "谢谢你愿意和我分享。你的感受很重要，我们可以一起慢慢梳理。",
            "嗯，我理解这种感觉。不管此刻的情绪是什么，它都值得被看见和接纳。",
            "如果愿意的话，可以试着把此刻的想法写进情绪日志，文字有时能帮我们看清内心。",
            "你并不孤单，我会一直在这里。还有什么想聊的吗？");

    private FallbackReplies() {
    }

    /** 关键词规则匹配（返回首个命中规则文案，未命中返回 null） */
    public static String matchRule(String text) {
        if (text == null) {
            return null;
        }
        return REPLY_RULES.stream()
                .filter(rule -> rule.keys().stream().anyMatch(text::contains))
                .map(Rule::text)
                .findFirst()
                .orElse(null);
    }
}
