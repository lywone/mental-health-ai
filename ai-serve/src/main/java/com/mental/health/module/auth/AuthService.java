package com.mental.health.module.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.config.AuthProperties;
import com.mental.health.config.JwtProperties;
import com.mental.health.config.JwtUtil;
import com.mental.health.module.auth.dto.LoginResponse;
import com.mental.health.module.auth.dto.SmsSendResponse;
import com.mental.health.module.user.User;
import com.mental.health.module.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 认证服务：管理员登录（失败锁定）/ 用户开发登录 / 登出黑名单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String LOGIN_LOCK_PREFIX = "login:lock:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);
    private static final Duration FAIL_DURATION = Duration.ofMinutes(10);

    private final SysAdminMapper sysAdminMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final SmsCodeService smsCodeService;
    private final AuthProperties authProperties;

    /** 管理员登录：连续失败 5 次锁定 10 分钟（Redis 计数） */
    public LoginResponse adminLogin(String username, String password) {
        String lockKey = LOGIN_LOCK_PREFIX + username;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "失败次数过多，账号已锁定，请10分钟后再试");
        }
        SysAdmin admin = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, username));
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            recordLoginFail(username);
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        stringRedisTemplate.delete(LOGIN_FAIL_PREFIX + username);

        admin.setLastLoginTime(LocalDateTime.now());
        sysAdminMapper.updateById(admin);

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getNickname());
        LoginResponse.AdminInfo adminInfo =
                new LoginResponse.AdminInfo(admin.getId(), admin.getUsername(), admin.getNickname());
        return new LoginResponse(token, "Bearer", jwtProperties.getAdminExpireSeconds(), adminInfo);
    }

    /** 用户端开发登录（受 auth.dev-login-enabled 开关控制）：userId 为空取最小 id 用户，表空则创建默认用户「小林」 */
    public LoginResponse devLogin(Long userId) {
        if (!authProperties.isDevLoginEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "开发登录已关闭，请使用手机号验证码登录");
        }
        User user;
        if (userId != null) {
            user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
            }
        } else {
            user = userMapper.selectList(
                            new LambdaQueryWrapper<User>().orderByAsc(User::getId).last("LIMIT 1"))
                    .stream().findFirst().orElse(null);
            if (user == null) {
                user = new User();
                user.setNickname("小林");
                user.setStatus(1);
                userMapper.insert(user);
            }
        }
        String token = jwtUtil.generateUserToken(user.getId(), user.getNickname());
        LoginResponse.UserInfo userInfo =
                new LoginResponse.UserInfo(user.getId(), user.getNickname(), user.getAvatar());
        return new LoginResponse(token, "Bearer", jwtProperties.getUserExpireSeconds(), userInfo);
    }

    /** 发送用户端短信验证码 */
    public SmsSendResponse sendSmsCode(String phone, String scene) {
        return smsCodeService.send(phone, scene);
    }

    /** 用户端注册：验证码校验 → 手机号查重 → 建号（昵称选填）→ 注册即登录 */
    public LoginResponse register(String phone, String code, String nickname) {
        smsCodeService.verify(phone, SmsCodeService.SCENE_REGISTER, code);
        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (exists != null) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        User user = new User();
        user.setPhone(phone);
        String name = nickname == null ? "" : nickname.trim();
        user.setNickname(name.isEmpty() ? "用户" + phone.substring(phone.length() - 4) : name);
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册触发 uk_phone 唯一键冲突
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        return buildUserLoginResponse(user);
    }

    /** 用户端验证码登录 */
    public LoginResponse smsLogin(String phone, String code) {
        smsCodeService.verify(phone, SmsCodeService.SCENE_LOGIN, code);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该手机号未注册，请先注册");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        return buildUserLoginResponse(user);
    }

    private LoginResponse buildUserLoginResponse(User user) {
        String token = jwtUtil.generateUserToken(user.getId(), user.getNickname());
        LoginResponse.UserInfo userInfo =
                new LoginResponse.UserInfo(user.getId(), user.getNickname(), user.getAvatar());
        return new LoginResponse(token, "Bearer", jwtProperties.getUserExpireSeconds(), userInfo);
    }

    /** 登出：token 加入 Redis 黑名单，TTL = 剩余有效期 */
    public void logout(String token) {
        long remaining = jwtUtil.remainingSeconds(token);
        if (remaining > 0) {
            stringRedisTemplate.opsForValue()
                    .set(com.mental.health.config.JwtAuthFilter.blacklistKey(token), "1",
                            Duration.ofSeconds(remaining));
        }
        log.info("token 已加入黑名单，剩余 {} 秒", remaining);
    }

    private void recordLoginFail(String username) {
        String failKey = LOGIN_FAIL_PREFIX + username;
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, FAIL_DURATION);
        }
        if (count != null && count >= MAX_FAIL_COUNT) {
            stringRedisTemplate.opsForValue().set(lockKey(username), "1", LOCK_DURATION);
            stringRedisTemplate.delete(failKey);
        }
    }

    private String lockKey(String username) {
        return LOGIN_LOCK_PREFIX + username;
    }
}
