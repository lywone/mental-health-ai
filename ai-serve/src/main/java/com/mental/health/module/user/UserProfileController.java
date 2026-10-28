package com.mental.health.module.user;

import com.mental.health.common.Result;
import com.mental.health.config.CurrentUser;
import com.mental.health.infra.storage.UploadController;
import com.mental.health.module.user.dto.ProfileUpdateRequest;
import com.mental.health.module.user.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端个人中心：查询/更新本人信息、头像上传（uid 取自登录态）
 */
@Tag(name = "用户端-个人中心")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @Operation(summary = "我的个人信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        return Result.ok(userService.getProfile(currentUser.uid()));
    }

    @Operation(summary = "更新个人信息（仅更新传入字段）")
    @PutMapping("/profile")
    public Result<UserProfileVO> update(@AuthenticationPrincipal CurrentUser currentUser,
                                        @Valid @RequestBody ProfileUpdateRequest request) {
        return Result.ok(userService.updateProfile(currentUser.uid(), request));
    }

    @Operation(summary = "头像上传（jpg/png/webp ≤5MB）")
    @PostMapping("/upload/avatar")
    public Result<UploadController.UploadVO> uploadAvatar(@AuthenticationPrincipal CurrentUser currentUser,
                                                          @RequestParam("file") MultipartFile file) {
        return Result.ok(new UploadController.UploadVO(userService.uploadAvatar(file)));
    }
}
