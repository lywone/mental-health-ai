package com.mental.health.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员登录请求
 */
@Data
public class AdminLoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 50, message = "用户名长度 1-50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 64, message = "密码长度 1-64")
    private String password;
}
