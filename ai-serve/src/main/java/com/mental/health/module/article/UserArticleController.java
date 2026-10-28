package com.mental.health.module.article;

import com.mental.health.common.PageResult;
import com.mental.health.common.Result;
import com.mental.health.module.article.vo.ArticleCardVO;
import com.mental.health.module.article.vo.ArticleDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库接口（用户端）
 */
@Tag(name = "知识库-用户端")
@RestController
@RequestMapping("/api/user/articles")
@RequiredArgsConstructor
public class UserArticleController {

    private final ArticleService articleService;

    @Operation(summary = "已发布文章列表")
    @GetMapping
    public Result<PageResult<ArticleCardVO>> page(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "6") long pageSize) {
        return Result.ok(articleService.pageForUser(category, page, pageSize));
    }

    @Operation(summary = "文章详情（浏览量自增）")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.detailForUser(id));
    }
}
