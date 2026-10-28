package com.mental.health.infra.ai;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 危机词检测：命中后不走 LLM，返回固定干预话术并打 risk_flag=1（词表以常量维护，便于调整）
 */
@Component
public class CrisisWordDetector {

    /** 危机敏感词表（自伤/轻生类） */
    public static final List<String> CRISIS_WORDS = List.of(
            "自杀", "轻生", "自残", "不想活", "活不下去", "想死", "结束生命", "结束自己的生命",
            "伤害自己", "了结自己", "割腕", "跳楼");

    /** 固定干预话术：肯定情绪 + 建议寻求专业帮助 / 心理援助热线 */
    public static final String CRISIS_REPLY =
            "我听到了你此刻的痛苦，谢谢你愿意说出来——你的感受非常重要，也值得被认真对待。"
                    + "这种时候请不要独自硬撑：可以现在就联系你信任的家人或朋友陪伴你，"
                    + "也可以拨打全国心理援助热线 12356，或前往就近医院的心理科/精神科寻求专业帮助。"
                    + "寻求帮助不是软弱，而是对自己负责。我会一直在这里，陪着你慢慢走出来。";

    public boolean hit(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return CRISIS_WORDS.stream().anyMatch(text::contains);
    }
}
