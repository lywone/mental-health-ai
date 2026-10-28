package com.mental.health.config;

/**
 * 当前登录用户（JWT claims 映射），作为 SecurityContext 的 principal
 */
public record CurrentUser(Long uid, String role, String nickname) {
}
