package com.mental.health.module.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.common.PageResult;
import com.mental.health.module.article.dto.ArticleSaveRequest;
import com.mental.health.module.article.vo.ArticleCardVO;
import com.mental.health.module.article.vo.ArticleDetailVO;
import com.mental.health.module.article.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 文章服务：管理端 CRUD/发布下架 + 用户端阅读
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;

    // ---------- 管理端 ----------

    public PageResult<ArticleVO> pageForAdmin(String keyword, String category, String status,
                                              long page, long pageSize) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Article::getTitle, keyword);
        }
        if (StringUtils.hasText(category)) {
            ArticleCategories.check(category);
            wrapper.eq(Article::getCategory, category);
        }
        ArticleStatus statusEnum = ArticleStatus.ofLabel(status);
        if (statusEnum != null) {
            wrapper.eq(Article::getStatus, statusEnum.getCode());
        }
        wrapper.orderByDesc(Article::getCreatedAt);
        Page<Article> result = articleMapper.selectPage(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.convert(this::toVO));
    }

    public ArticleDetailVO detail(Long id) {
        Article article = requireArticle(id);
        ArticleDetailVO vo = new ArticleDetailVO();
        copyVO(article, vo);
        vo.setContent(article.getContent());
        return vo;
    }

    /** 新增：固定写入草稿，views=0，publishTime=null */
    public ArticleVO create(ArticleSaveRequest request) {
        ArticleCategories.check(request.getCategory());
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setCoverUrl(request.getCover());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setStatus(ArticleStatus.DRAFT.getCode());
        article.setViews(0);
        article.setPublishTime(null);
        articleMapper.insert(article);
        return toVO(article);
    }

    /** 编辑：任意状态可编辑，仅更新内容字段 */
    public ArticleVO update(Long id, ArticleSaveRequest request) {
        Article article = requireArticle(id);
        ArticleCategories.check(request.getCategory());
        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setCoverUrl(request.getCover());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        articleMapper.updateById(article);
        return toVO(article);
    }

    /** 物理删除 */
    public void delete(Long id) {
        requireArticle(id);
        articleMapper.deleteById(id);
    }

    /**
     * 发布/下架（幂等）：
     * publish → 置为已发布，publishTime 为空时写当前时间（下架后重新发布不改首次发布时间）；
     * offline → 置为已下架，publishTime 保留。
     */
    public ArticleVO changeStatus(Long id, String action) {
        Article article = requireArticle(id);
        switch (action) {
            case "publish" -> {
                article.setStatus(ArticleStatus.PUBLISHED.getCode());
                if (article.getPublishTime() == null) {
                    article.setPublishTime(LocalDateTime.now());
                }
            }
            case "offline" -> article.setStatus(ArticleStatus.OFFLINE.getCode());
            default -> throw new BusinessException(ErrorCode.PARAM_ERROR, "action 仅支持 publish / offline");
        }
        articleMapper.updateById(article);
        return toVO(article);
    }

    // ---------- 用户端 ----------

    /** 仅已发布，按 publish_time 倒序 */
    public PageResult<ArticleCardVO> pageForUser(String category, long page, long pageSize) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatus.PUBLISHED.getCode());
        if (StringUtils.hasText(category) && !"全部".equals(category)) {
            ArticleCategories.check(category);
            wrapper.eq(Article::getCategory, category);
        }
        wrapper.orderByDesc(Article::getPublishTime);
        Page<Article> result = articleMapper.selectPage(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.convert(this::toCard));
    }

    /** 用户端详情：仅已发布可见（否则 1102），views 原子自增后返回 */
    public ArticleDetailVO detailForUser(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || !Objects.equals(article.getStatus(), ArticleStatus.PUBLISHED.getCode())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文章不存在或未发布");
        }
        articleMapper.incrementViews(id);
        article.setViews(article.getViews() == null ? 1 : article.getViews() + 1);
        ArticleDetailVO vo = new ArticleDetailVO();
        copyVO(article, vo);
        vo.setContent(article.getContent());
        return vo;
    }

    // ---------- 转换 ----------

    private Article requireArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文章不存在");
        }
        return article;
    }

    private ArticleVO toVO(Article article) {
        ArticleVO vo = new ArticleVO();
        copyVO(article, vo);
        return vo;
    }

    private void copyVO(Article article, ArticleVO vo) {
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCategory(article.getCategory());
        vo.setCover(article.getCoverUrl() == null ? "" : article.getCoverUrl());
        vo.setSummary(article.getSummary());
        ArticleStatus status = ArticleStatus.ofCode(article.getStatus());
        vo.setStatus(status == null ? "" : status.getLabel());
        vo.setViews(article.getViews());
        vo.setPublishTime(article.getPublishTime());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
    }

    private ArticleCardVO toCard(Article article) {
        ArticleCardVO vo = new ArticleCardVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCategory(article.getCategory());
        vo.setCover(article.getCoverUrl() == null ? "" : article.getCoverUrl());
        vo.setSummary(article.getSummary());
        vo.setViews(article.getViews());
        vo.setPublishTime(article.getPublishTime());
        return vo;
    }
}
