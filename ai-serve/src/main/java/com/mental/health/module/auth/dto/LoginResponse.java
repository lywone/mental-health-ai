package com.mental.health.module.auth.dto;

/**
 * 登录响应（token + 用户/管理员信息）
 */
public record LoginResponse(String token, String tokenType, long expiresIn, Object profile) {

    /** 管理员信息 */
    public record AdminInfo(Long id, String username, String nickname) {
    }

    /** 用户信息 */
    public record UserInfo(Long id, String nickname, String avatar) {
    }
}
