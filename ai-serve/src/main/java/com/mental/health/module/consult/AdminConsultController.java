package com.mental.health.module.consult;

import com.mental.health.common.PageResult;
import com.mental.health.common.Result;
import com.mental.health.config.CurrentUser;
import com.mental.health.module.consult.vo.ConsultSessionDetailVO;
import com.mental.health.module.consult.vo.ConsultSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 咨询记录接口（管理后台，只读）
 */
@Slf4j
@Tag(name = "咨询记录-管理端")
@RestController
@RequestMapping("/api/admin/consultations")
@RequiredArgsConstructor
public class AdminConsultController {

    private final ConsultService consultService;

    @Operation(summary = "分页列表")
    @GetMapping
    public Result<PageResult<ConsultSessionVO>> page(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(consultService.pageForAdmin(user, startDate, endDate, status, page, pageSize));
    }

    @Operation(summary = "会话详情（含消息，敏感数据）")
    @GetMapping("/{id}")
    public Result<ConsultSessionDetailVO> detail(@AuthenticationPrincipal CurrentUser currentUser,
                                                 @PathVariable Long id) {
        // 敏感数据访问审计日志（v1.1 落库 sys_audit_log）
        log.info("[审计] 管理员[{}] 查看会话详情 id={}", currentUser.nickname(), id);
        return Result.ok(consultService.detailForAdmin(id));
    }
}
