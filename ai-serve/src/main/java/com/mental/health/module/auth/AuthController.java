package com.mental.health.module.auth;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.common.Result;
import com.mental.health.config.CurrentUser;
import com.mental.health.config.JwtAuthFilter;
import com.mental.health.module.auth.dto.AdminLoginRequest;
import com.mental.health.module.auth.dto.DevLoginRequest;
import com.mental.health.module.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：管理员登录/登出/信息 + 用户端开发登录
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysAdminMapper sysAdminMapper;

    @Operation(summary = "管理员登录")
    @PostMapping("/admin/login")
    public Result<LoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return Result.ok(authService.adminLogin(request.getUsername(), request.getPassword()));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/admin/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute(JwtAuthFilter.TOKEN_ATTR);
        if (token != null) {
            authService.logout(token);
        }
        return Result.ok();
    }

    @Operation(summary = "当前管理员信息")
    @GetMapping("/admin/me")
    public Result<LoginResponse.AdminInfo> me(@AuthenticationPrincipal CurrentUser currentUser) {
        SysAdmin admin = sysAdminMapper.selectById(currentUser.uid());
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return Result.ok(new LoginResponse.AdminInfo(admin.getId(), admin.getUsername(), admin.getNickname()));
    }

    @Operation(summary = "用户端开发登录")
    @PostMapping("/user/dev-login")
    public Result<LoginResponse> devLogin(@RequestBody(required = false) DevLoginRequest request) {
        Long userId = request == null ? null : request.getUserId();
        return Result.ok(authService.devLogin(userId));
    }
}
