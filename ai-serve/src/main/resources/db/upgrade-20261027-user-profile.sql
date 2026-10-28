-- ============================================================
-- 增量升级：用户注册与个人中心（设计文档 v1.0 · 2026-10-27）
-- 已用 init.sql 全新初始化的库无需执行本脚本
-- ============================================================
USE mental_health;

ALTER TABLE t_user
    ADD COLUMN gender   TINYINT      NULL COMMENT '0 保密 / 1 男 / 2 女' AFTER avatar,
    ADD COLUMN birthday DATE         NULL COMMENT '生日' AFTER gender,
    ADD COLUMN bio      VARCHAR(200) NULL COMMENT '个人简介' AFTER birthday;
