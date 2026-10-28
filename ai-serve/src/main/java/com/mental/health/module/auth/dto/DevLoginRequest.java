package com.mental.health.module.auth.dto;

import lombok.Data;

/**
 * 用户端开发登录请求（userId 可空）
 */
@Data
public class DevLoginRequest {

    /** 指定用户 id；为空取 t_user 最小 id 用户 */
    private Long userId;
}
