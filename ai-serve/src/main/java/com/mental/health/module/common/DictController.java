package com.mental.health.module.common;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.common.Result;
import com.mental.health.module.article.ArticleCategories;
import com.mental.health.module.mood.MoodTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共字典接口（登录即可访问）：type = article_category | mood_tag
 */
@Tag(name = "公共字典")
@RestController
@RequestMapping("/api/common")
public class DictController {

    private static final String TYPE_ARTICLE_CATEGORY = "article_category";
    private static final String TYPE_MOOD_TAG = "mood_tag";

    @Operation(summary = "字典（type 为空返回全部）")
    @GetMapping("/dicts")
    public Result<Map<String, List<String>>> dicts(@RequestParam(required = false) String type) {
        Map<String, List<String>> dicts = new LinkedHashMap<>();
        if (!StringUtils.hasText(type)) {
            dicts.put(TYPE_ARTICLE_CATEGORY, ArticleCategories.ALL);
            dicts.put(TYPE_MOOD_TAG, MoodTags.ALL);
            return Result.ok(dicts);
        }
        switch (type) {
            case TYPE_ARTICLE_CATEGORY -> dicts.put(TYPE_ARTICLE_CATEGORY, ArticleCategories.ALL);
            case TYPE_MOOD_TAG -> dicts.put(TYPE_MOOD_TAG, MoodTags.ALL);
            default -> throw new BusinessException(ErrorCode.PARAM_ERROR,
                    "type 仅支持 " + TYPE_ARTICLE_CATEGORY + " / " + TYPE_MOOD_TAG);
        }
        return Result.ok(dicts);
    }
}
