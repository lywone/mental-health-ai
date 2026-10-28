package com.mental.health.module.consult;

import com.mental.health.common.Result;
import com.mental.health.config.CurrentUser;
import com.mental.health.module.consult.dto.ChatSendRequest;
import com.mental.health.module.consult.dto.SessionScoreRequest;
import com.mental.health.module.consult.vo.ChatReplyVO;
import com.mental.health.module.consult.vo.ConsultSessionDetailVO;
import com.mental.health.module.consult.vo.ConsultSessionVO;
import com.mental.health.module.consult.vo.UserSessionItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 AI 咨询接口：会话历史 / 发送消息 / 结束会话 / 评分
 */
@Tag(name = "AI咨询-用户端")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserChatController {

    private final ConsultService consultService;
    private final ChatService chatService;

    @Operation(summary = "我的会话历史")
    @GetMapping("/consultations")
    public Result<List<UserSessionItemVO>> mySessions(@AuthenticationPrincipal CurrentUser currentUser) {
        return Result.ok(consultService.listMine(currentUser.uid()));
    }

    @Operation(summary = "我的会话详情（含消息，仅本人）")
    @GetMapping("/consultations/{id}")
    public Result<ConsultSessionDetailVO> mySessionDetail(@AuthenticationPrincipal CurrentUser currentUser,
                                                          @PathVariable Long id) {
        return Result.ok(consultService.detailMine(currentUser.uid(), id));
    }

    @Operation(summary = "发送消息（无会话则自动新建）")
    @PostMapping("/chat/messages")
    public Result<ChatReplyVO> sendMessage(@AuthenticationPrincipal CurrentUser currentUser,
                                           @Valid @RequestBody ChatSendRequest request) {
        return Result.ok(chatService.sendMessage(currentUser.uid(), request));
    }

    @Operation(summary = "结束会话（幂等）")
    @PostMapping("/consultations/{id}/end")
    public Result<ConsultSessionVO> end(@AuthenticationPrincipal CurrentUser currentUser,
                                        @PathVariable Long id) {
        return Result.ok(consultService.endSession(currentUser.uid(), id));
    }

    @Operation(summary = "会话评分（1-10，仅已结束）")
    @PutMapping("/consultations/{id}/score")
    public Result<ConsultSessionVO> score(@AuthenticationPrincipal CurrentUser currentUser,
                                          @PathVariable Long id,
                                          @Valid @RequestBody SessionScoreRequest request) {
        return Result.ok(consultService.score(currentUser.uid(), id, request.getScore()));
    }
}
