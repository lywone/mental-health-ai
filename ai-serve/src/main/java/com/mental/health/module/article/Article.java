package com.mental.health.module.article;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文章（t_article）
 */
@Data
@TableName("t_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String coverUrl;

    private String summary;

    private String content;

    /** 1 草稿 / 2 已发布 / 3 已下架 */
    private Integer status;

    private Integer views;

    /** 首次发布时间，下架不清空 */
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
