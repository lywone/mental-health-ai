package com.mental.health.module.article;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 文章 Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /** 浏览量原子自增，避免读改写竞态 */
    @Update("UPDATE t_article SET views = views + 1 WHERE id = #{id}")
    int incrementViews(@Param("id") Long id);
}
