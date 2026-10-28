package com.mental.health.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户端发送验证码请求
 */
@Data
public class UserSmsSendRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 场景：LOGIN / REGISTER */
    @NotBlank(message = "验证码场景不能为空")
    private String scene;
}
