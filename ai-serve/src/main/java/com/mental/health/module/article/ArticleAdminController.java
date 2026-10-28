package com.mental.health.module.article;

import com.mental.health.common.PageResult;
import com.mental.health.common.Result;
import com.mental.health.module.article.dto.ArticleSaveRequest;
import com.mental.health.module.article.dto.ArticleStatusRequest;
import com.mental.health.module.article.vo.ArticleDetailVO;
import com.mental.health.module.article.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章管理接口（管理后台）
 */
@Tag(name = "文章管理-管理端")
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class ArticleAdminController {

    private final ArticleService articleService;

    @Operation(summary = "分页列表")
    @GetMapping
    public Result<PageResult<ArticleVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(articleService.pageForAdmin(keyword, category, status, page, pageSize));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.detail(id));
    }

    @Operation(summary = "新增（固定草稿）")
    @PostMapping
    public Result<ArticleVO> create(@Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.create(request));
    }

    @Operation(summary = "编辑（任意状态可编辑）")
    @PutMapping("/{id}")
    public Result<ArticleVO> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.update(id, request));
    }

    @Operation(summary = "删除（物理删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "发布 / 下架")
    @PutMapping("/{id}/status")
    public Result<ArticleVO> changeStatus(@PathVariable Long id,
                                          @Valid @RequestBody ArticleStatusRequest request) {
        return Result.ok(articleService.changeStatus(id, request.getAction()));
    }
}
