package com.mental.health.module.mood;

import com.mental.health.common.PageResult;
import com.mental.health.common.Result;
import com.mental.health.config.CurrentUser;
import com.mental.health.module.mood.dto.MoodCreateRequest;
import com.mental.health.module.mood.vo.EmotionGardenVO;
import com.mental.health.module.mood.vo.MoodLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 情绪日志接口（用户端）：记录 / 我的记录 / 情绪花园
 */
@Tag(name = "情绪日志-用户端")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserMoodController {

    private final MoodService moodService;

    @Operation(summary = "情绪花园（今日最新一条）")
    @GetMapping("/emotion-garden")
    public Result<EmotionGardenVO> emotionGarden(@AuthenticationPrincipal CurrentUser currentUser) {
        return Result.ok(moodService.emotionGarden(currentUser.uid()));
    }

    @Operation(summary = "记录心情")
    @PostMapping("/moods")
    public Result<MoodLogVO> create(@AuthenticationPrincipal CurrentUser currentUser,
                                    @Valid @RequestBody MoodCreateRequest request) {
        return Result.ok(moodService.create(currentUser.uid(), request));
    }

    @Operation(summary = "我的记录")
    @GetMapping("/moods")
    public Result<PageResult<MoodLogVO>> page(@AuthenticationPrincipal CurrentUser currentUser,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(moodService.pageForUser(currentUser.uid(), page, pageSize));
    }
}
