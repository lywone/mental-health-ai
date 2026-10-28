package com.mental.health.module.user.vo;

/**
 * 用户个人信息（手机号脱敏返回）
 */
public record UserProfileVO(Long id, String phone, String nickname, String avatar, Integer gender,
                            String birthday, String bio, String createdAt) {
}
