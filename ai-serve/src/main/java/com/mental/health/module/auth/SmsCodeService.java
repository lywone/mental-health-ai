package com.mental.health.module.auth;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.infra.sms.SmsProperties;
import com.mental.health.infra.sms.SmsSender;
import com.mental.health.module.auth.dto.SmsSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 短信验证码：发送频控（60s 间隔 / 每日上限）+ 校验（错误锁定 / 一次性消费），Redis 存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    /** 支持的业务场景：登录 / 注册（验证码按场景隔离） */
    public static final String SCENE_LOGIN = "LOGIN";
    public static final String SCENE_REGISTER = "REGISTER";
    private static final Set<String> SCENES = Set.of(SCENE_LOGIN, SCENE_REGISTER);

    private static final String CODE_KEY = "sms:code:%s:%s";        // sms:code:{scene}:{phone}
    private static final String INTERVAL_KEY = "sms:interval:%s";    // sms:interval:{phone}
    private static final String DAILY_KEY = "sms:limit:%s:%s";       // sms:limit:{phone}:{yyyyMMdd}
    private static final String FAIL_KEY = "sms:fail:%s";            // sms:fail:{phone}
    private static final String LOCK_KEY = "sms:lock:%s";            // sms:lock:{phone}
    private static final int MAX_FAIL_COUNT = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final SmsSender smsSender;
    private final SmsProperties smsProperties;

    /** 发送验证码（频控通过后生成 6 位码存 Redis 并走通道发送） */
    public SmsSendResponse send(String phone, String scene) {
        if (!SCENES.contains(scene)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码场景非法");
        }
        String intervalKey = String.format(INTERVAL_KEY, phone);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(intervalKey))) {
            throw new BusinessException(ErrorCode.SMS_TOO_FREQUENT);
        }
        String dailyKey = String.format(DAILY_KEY, phone, LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        Long sent = stringRedisTemplate.opsForValue().increment(dailyKey);
        if (sent != null && sent == 1) {
            stringRedisTemplate.expire(dailyKey, Duration.ofDays(1));
        }
        if (sent != null && sent > smsProperties.getDailyLimit()) {
            throw new BusinessException(ErrorCode.SMS_SEND_LIMIT);
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        stringRedisTemplate.opsForValue().set(
                String.format(CODE_KEY, scene, phone), code, Duration.ofSeconds(smsProperties.getCodeTtlSeconds()));
        stringRedisTemplate.opsForValue().set(intervalKey, "1", Duration.ofSeconds(smsProperties.getSendIntervalSeconds()));

        smsSender.send(phone, code, scene);
        log.info("验证码已发送: phone={}, scene={}", phone, scene);
        boolean echo = smsProperties.isEchoDevCode() && "log".equals(smsProperties.getChannel());
        return new SmsSendResponse(smsProperties.getSendIntervalSeconds(), echo ? code : null);
    }

    /** 校验验证码：通过后立即删除（一次性）；连续错误 5 次锁定 30 分钟 */
    public void verify(String phone, String scene, String code) {
        String lockKey = String.format(LOCK_KEY, phone);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            throw new BusinessException(ErrorCode.SMS_CODE_INVALID, "失败次数过多，请30分钟后再试");
        }
        String codeKey = String.format(CODE_KEY, scene, phone);
        String stored = stringRedisTemplate.opsForValue().get(codeKey);
        if (stored == null || !stored.equals(code)) {
            recordFail(phone);
            throw new BusinessException(ErrorCode.SMS_CODE_INVALID);
        }
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(String.format(FAIL_KEY, phone));
    }

    private void recordFail(String phone) {
        String failKey = String.format(FAIL_KEY, phone);
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, LOCK_DURATION);
        }
        if (count != null && count >= MAX_FAIL_COUNT) {
            stringRedisTemplate.opsForValue().set(String.format(LOCK_KEY, phone), "1", LOCK_DURATION);
            stringRedisTemplate.delete(failKey);
        }
    }
}
