package com.mental.health.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端注册请求（仅手机号 + 验证码必填，昵称选填）
 */
@Data
public class UserRegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码为 6 位数字")
    private String code;

    /** 选填，为空时默认「用户+手机号后4位」 */
    @Size(max = 20, message = "昵称不超过 20 字")
    private String nickname;
}
