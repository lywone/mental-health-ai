package com.mental.health.module.auth;

import com.mental.health.common.Result;
import com.mental.health.module.auth.dto.LoginResponse;
import com.mental.health.module.auth.dto.SmsSendResponse;
import com.mental.health.module.auth.dto.UserLoginRequest;
import com.mental.health.module.auth.dto.UserRegisterRequest;
import com.mental.health.module.auth.dto.UserSmsSendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证接口：短信验证码发送 / 注册 / 登录（匿名可访问）
 */
@Tag(name = "认证-用户端")
@RestController
@RequestMapping("/api/auth/user")
@RequiredArgsConstructor
public class UserAuthController {

    private final AuthService authService;

    @Operation(summary = "发送短信验证码（scene: LOGIN / REGISTER）")
    @PostMapping("/sms/send")
    public Result<SmsSendResponse> smsSend(@Valid @RequestBody UserSmsSendRequest request) {
        return Result.ok(authService.sendSmsCode(request.getPhone(), request.getScene()));
    }

    @Operation(summary = "用户注册（手机号+验证码，成功即登录）")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return Result.ok(authService.register(request.getPhone(), request.getCode(), request.getNickname()));
    }

    @Operation(summary = "用户登录（手机号+验证码）")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return Result.ok(authService.smsLogin(request.getPhone(), request.getCode()));
    }
}
