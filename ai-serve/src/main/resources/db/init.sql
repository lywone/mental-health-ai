-- ============================================================
-- 心理健康AI助手 · 数据库初始化脚本（MySQL 8.0，utf8mb4）
-- 表结构见《后端接口设计文档 v1.0》3.2；种子数据文案与 src/mock/data.js 对齐
-- ============================================================

CREATE DATABASE IF NOT EXISTS mental_health DEFAULT CHARACTER SET utf8mb4;
USE mental_health;

-- ------------------------------------------------------------
-- 1. 管理员（默认 admin / 123456 由后端 AdminInitializer 启动时自动创建，BCrypt 加密）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_admin (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password        VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密',
    nickname        VARCHAR(50)  NULL COMMENT '显示名',
    last_login_time DATETIME     NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员账号';

-- ------------------------------------------------------------
-- 2. C端用户
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_user (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '对外即用户 ID',
    nickname   VARCHAR(50)  NOT NULL COMMENT '昵称',
    avatar     VARCHAR(255) NULL COMMENT '头像 URL',
    gender     TINYINT      NULL COMMENT '0 保密 / 1 男 / 2 女',
    birthday   DATE         NULL COMMENT '生日',
    bio        VARCHAR(200) NULL COMMENT '个人简介',
    phone      VARCHAR(20)  NULL COMMENT '手机号（注册/登录账号）',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 / 0 禁用',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'C端用户';

-- ------------------------------------------------------------
-- 3. 情绪日志
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_mood_log (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    score      TINYINT     NOT NULL COMMENT '情绪评分 1-10',
    tags       VARCHAR(64) NULL COMMENT '逗号分隔，最多 2 个',
    content    VARCHAR(200) NULL COMMENT '心情文字，可为空',
    session_id BIGINT      NULL COMMENT '预留：来源咨询会话',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_created (user_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '情绪日志';

-- ------------------------------------------------------------
-- 4. 咨询会话
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_consult_session (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    status        TINYINT  NOT NULL DEFAULT 1 COMMENT '1 进行中 / 2 已结束',
    start_time    DATETIME NOT NULL COMMENT '首条消息时间',
    end_time      DATETIME NULL,
    duration_min  INT      NULL COMMENT '时长（分钟，≥1），结束时计算',
    message_count INT      NOT NULL DEFAULT 0 COMMENT '冗余消息数，事务内维护',
    score         TINYINT  NULL COMMENT '会话评分 1-10',
    risk_flag     TINYINT  NOT NULL DEFAULT 0 COMMENT '危机干预标记 0/1',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '咨询会话';

-- ------------------------------------------------------------
-- 5. 咨询消息
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_consult_message (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    session_id BIGINT        NOT NULL,
    from_type  TINYINT       NOT NULL COMMENT '1 用户 / 2 AI（VO 输出 user / ai）',
    content    VARCHAR(2000) NOT NULL COMMENT '用户输入 ≤500，AI 回复可能较长',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_session_created (session_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '咨询消息';

-- ------------------------------------------------------------
-- 6. 知识文章
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_article (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(50)  NOT NULL COMMENT '标题 ≤50 字',
    category     VARCHAR(20)  NOT NULL COMMENT '分类',
    cover_url    VARCHAR(255) NULL COMMENT '封面图 URL，可空',
    summary      VARCHAR(100) NULL COMMENT '摘要 ≤100，可空',
    content      TEXT         NULL COMMENT '正文纯文本',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1 草稿 / 2 已发布 / 3 已下架',
    views        INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    publish_time DATETIME     NULL COMMENT '首次发布时间，下架不清空',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category_status (category, status),
    KEY idx_publish_time (publish_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '知识文章';

-- ============================================================
-- 种子数据（INSERT 显式 id；时间基于导入当天动态生成，保证统计口径自洽）
-- ============================================================

-- ---------- 用户：小林 / 阿哲 / 叶叶（对应 Mock U1001/U1002/U1003） ----------
INSERT INTO t_user (id, nickname, avatar, phone, status, created_at) VALUES
(1, '小林', NULL, NULL, 1, NOW() - INTERVAL 35 DAY),
(2, '阿哲', NULL, NULL, 1, NOW() - INTERVAL 34 DAY),
(3, '叶叶', NULL, NULL, 1, NOW() - INTERVAL 30 DAY);

-- ---------- 知识文章：12 篇（前 8 已发布 / 2 草稿 / 2 已下架，标题与分类照 Mock ARTICLE_SEEDS） ----------
SET @p1 = '在快节奏的生活中，情绪波动是每个人都会经历的自然现象。情绪本身没有好坏之分，它们更像是一封封信使，提醒我们内心的需要。';
SET @p2 = '第一步是觉察。当我们能够给情绪命名——「我现在感到焦虑」——大脑的杏仁核活跃度就会显著下降，这是情绪调节的开始。';
SET @p3 = '其次，尝试为情绪留出空间。不必急着摆脱它，可以问自己：这份感受想告诉我什么？是疲惫、委屈，还是未被满足的期待？';
SET @p4 = '研究显示，规律作息、适度运动与良好的社会支持，是心理韧性的三大基石。哪怕是每天十分钟的散步，也在悄悄重塑你的情绪基线。';
SET @p5 = '最后，请记住：寻求帮助是勇敢者的选择。当自我调节难以奏效时，向信任的人倾诉或寻求专业支持，都是对自己负责的方式。';
SET @p6 = '愿你在每一个起伏的日子里，都能温柔地接住自己。';
SET @summary = '在快节奏的生活中，情绪波动是每个人都会经历的自然现象。情绪本身没有好坏之分，它们更像是一封封信使，提醒我们内心的需要…';

INSERT INTO t_article (id, title, category, cover_url, summary, content, status, views, publish_time, created_at) VALUES
(1,  '认识情绪：情绪不是敌人，而是信使', '情绪管理', '', @summary, CONCAT_WS(CHAR(10), @p1, @p2, @p3, @p4, @p6), 2, 128, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 15:30:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 10:00:00')),
(2,  '四步走出情绪低谷：STOP 情绪急救法', '情绪管理', '', @summary, CONCAT_WS(CHAR(10), @p1, @p2, @p3, @p5, @p6), 2, 96, CONCAT(DATE_SUB(CURDATE(), INTERVAL 3 DAY), ' 10:15:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 09:00:00')),
(3,  '为什么我们会焦虑？焦虑的自我觉察', '情绪管理', '', @summary, CONCAT_WS(CHAR(10), @p1, @p2, @p4, @p6), 2, 210, CONCAT(DATE_SUB(CURDATE(), INTERVAL 5 DAY), ' 18:40:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), ' 14:00:00')),
(4,  '科学睡眠指南：把失眠还给夜晚', '睡眠健康', '', @summary, CONCAT_WS(CHAR(10), @p1, @p3, @p4, @p5, @p6), 2, 287, CONCAT(DATE_SUB(CURDATE(), INTERVAL 8 DAY), ' 09:50:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 9 DAY), ' 16:00:00')),
(5,  '睡前一小时：打造你的入睡仪式感', '睡眠健康', '', @summary, CONCAT_WS(CHAR(10), @p1, @p2, @p5, @p6), 2, 54, CONCAT(DATE_SUB(CURDATE(), INTERVAL 11 DAY), ' 20:05:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 11:00:00')),
(6,  '什么是心理健康？破除三个常见误区', '心理科普', '', @summary, CONCAT_WS(CHAR(10), @p1, @p3, @p4, @p6), 2, 167, CONCAT(DATE_SUB(CURDATE(), INTERVAL 15 DAY), ' 11:25:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 16 DAY), ' 10:00:00')),
(7,  '压力的运作机制：读懂身体的信号', '心理科普', '', @summary, CONCAT_WS(CHAR(10), @p1, @p2, @p3, @p6), 2, 89, CONCAT(DATE_SUB(CURDATE(), INTERVAL 19 DAY), ' 16:10:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 20 DAY), ' 09:30:00')),
(8,  '人际关系中的边界感：温和而坚定', '人际关系', '', @summary, CONCAT_WS(CHAR(10), @p1, @p4, @p5, @p6), 2, 232, CONCAT(DATE_SUB(CURDATE(), INTERVAL 25 DAY), ' 14:45:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 26 DAY), ' 15:00:00')),
(9,  '如何好好吵架：亲密关系中的非暴力沟通', '人际关系', '', @summary, CONCAT_WS(CHAR(10), @p1, @p2, @p3), 1, 0, NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 13:00:00')),
(10, '正念入门：把注意力温柔地拉回当下', '情绪管理', '', @summary, CONCAT_WS(CHAR(10), @p1, @p4), 1, 0, NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 08:30:00')),
(11, '社交倦怠：为什么越休息越累', '心理科普', '', @summary, CONCAT_WS(CHAR(10), @p1, @p2, @p5), 3, 76, CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 13:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 19 DAY), ' 10:00:00')),
(12, '告别报复性熬夜：找回睡眠主动权', '睡眠健康', '', @summary, CONCAT_WS(CHAR(10), @p1, @p3, @p6), 3, 145, CONCAT(DATE_SUB(CURDATE(), INTERVAL 22 DAY), ' 17:20:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 12:00:00'));

-- ---------- 情绪日志：25 条（今日 2 条 + 近 29 天 23 条，覆盖 low/mid/high 三档） ----------
INSERT INTO t_mood_log (id, user_id, score, tags, content, session_id, created_at) VALUES
(1,  1, 8, '平静',        '今天状态不错，按时完成了工作计划，晚上打算奖励自己一部电影。', NULL, CONCAT(CURDATE(), ' 08:15:00')),
(2,  2, 5, '焦虑,疲惫',   '论文进度落后，有点焦虑，晚上去操场跑了三公里舒缓一下。', NULL, CONCAT(CURDATE(), ' 12:40:00')),
(3,  1, 3, '低落',        '开会时被当众批评了，心情有点低落，写下来梳理一下情绪。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 06:40:00')),
(4,  2, 2, '疲惫',        '失眠到凌晨两点，翻来覆去睡不着，脑子里全是没做完的事。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 22:10:00')),
(5,  3, 4, '焦虑',        '论文进度落后，有点焦虑，晚上去操场跑了三公里舒缓一下。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 5 DAY), ' 09:20:00')),
(6,  1, 7, '开心',        '和朋友聊了很久，把憋在心里的话都说出来了，感觉轻松多了。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 20:05:00')),
(7,  2, 5, '疲惫',        '连续加班一周，感觉非常疲惫，效率也越来越低。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 8 DAY), ' 13:30:00')),
(8,  3, 6, '平静',        '今天没有什么特别的事，很平静的一天，给这样的日子打个分。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 15 DAY), ' 21:15:00')),
(9,  1, 8, '开心,平静',   '上午效率很高，给自己点了杯咖啡，心情不错。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 20 DAY), ' 07:45:00')),
(10, 2, 1, '低落',        '面试被拒了，开始怀疑自己的能力，需要重新整理简历和心态。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 23:05:00')),
(11, 3, 9, '开心',        '运动之后心情好了一些，决定把晨跑坚持下去。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 9 DAY), ' 18:40:00')),
(12, 1, 6, '疲惫',        '最近总是睡不好，白天也没精神，希望这个周期能快点过去。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 3 DAY), ' 12:00:00')),
(13, 2, 2, '愤怒',        '和家人吵架了，很愤怒也很委屈，需要冷静下来再沟通。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 16:25:00')),
(14, 3, 10, '开心',       '今天状态不错，按时完成了工作计划，晚上打算奖励自己一部电影。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 25 DAY), ' 10:30:00')),
(15, 1, 5, '平静',        '和朋友聊了很久，把憋在心里的话都说出来了，感觉轻松多了。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 10 DAY), ' 19:50:00')),
(16, 2, 4, '焦虑',        '论文进度落后，有点焦虑，晚上去操场跑了三公里舒缓一下。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), ' 08:55:00')),
(17, 3, 3, '低落',        '失眠到凌晨两点，翻来覆去睡不着，脑子里全是没做完的事。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 14 DAY), ' 22:35:00')),
(18, 1, 7, '平静',        '今天没有什么特别的事，很平静的一天，给这样的日子打个分。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 17 DAY), ' 15:10:00')),
(19, 2, 8, '开心',        '上午效率很高，给自己点了杯咖啡，心情不错。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 22 DAY), ' 11:20:00')),
(20, 3, 6, '疲惫',        '连续加班一周，感觉非常疲惫，效率也越来越低。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 20:45:00')),
(21, 1, 9, '开心,平静',   '运动之后心情好了一些，决定把晨跑坚持下去。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 28 DAY), ' 09:05:00')),
(22, 2, 5, '愤怒',        '和家人吵架了，很愤怒也很委屈，需要冷静下来再沟通。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 13 DAY), ' 17:35:00')),
(23, 3, 2, '低落',        '面试被拒了，开始怀疑自己的能力，需要重新整理简历和心态。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 11 DAY), ' 21:55:00')),
(24, 1, 7, '开心',        '和朋友聊了很久，把憋在心里的话都说出来了，感觉轻松多了。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 24 DAY), ' 14:15:00')),
(25, 2, 4, '疲惫',        '最近总是睡不好，白天也没精神，希望这个周期能快点过去。', NULL, CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 19:30:00'));

-- ---------- 咨询会话：8 个（今日 1 场进行中 + 近 29 天 7 场已结束带时长与评分；消息照 Mock DIALOG_TEMPLATES 精简为 6 条/场） ----------
INSERT INTO t_consult_session (id, user_id, status, start_time, end_time, duration_min, message_count, score, risk_flag, created_at) VALUES
(1, 1, 1, CONCAT(CURDATE(), ' 09:15:00'), NULL, NULL, 6, NULL, 0, CONCAT(CURDATE(), ' 09:15:00')),
(2, 2, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:05:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:20:00'), 15, 6, 8, 0, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:05:00')),
(3, 3, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:12:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:28:00'), 16, 6, 6, 0, CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:12:00')),
(4, 1, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:30:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:45:00'), 15, 6, 9, 0, CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:30:00')),
(5, 2, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:47:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 11:02:00'), 15, 6, 7, 0, CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:47:00')),
(6, 3, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:20:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:36:00'), 16, 6, 5, 0, CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:20:00')),
(7, 1, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:03:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:19:00'), 16, 6, 4, 0, CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:03:00')),
(8, 2, 2, CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:11:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:27:00'), 16, 6, 10, 0, CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:11:00'));

INSERT INTO t_consult_message (id, session_id, from_type, content, created_at) VALUES
-- 会话1（今日，进行中，模板：睡眠）
(1, 1, 1, '最近总是睡不好，躺在床上翻来覆去一两个小时都睡不着。', CONCAT(CURDATE(), ' 09:16:00')),
(2, 1, 2, '你好，愿意和我多聊聊最近的睡眠情况吗？大概是从什么时候开始的？', CONCAT(CURDATE(), ' 09:18:00')),
(3, 1, 1, '嗯，大概两周了吧，晚上要很久才能睡着。', CONCAT(CURDATE(), ' 09:20:00')),
(4, 1, 2, '两周里有没有哪一晚相对好一些？当时和平时有什么不一样吗？', CONCAT(CURDATE(), ' 09:22:00')),
(5, 1, 1, '周末运动过之后那晚睡得还行。', CONCAT(CURDATE(), ' 09:24:00')),
(6, 1, 2, '这是个很好的线索。规律运动对睡眠有帮助，我们可以从每天 20 分钟的散步开始试试。', CONCAT(CURDATE(), ' 09:26:00')),
-- 会话2（-1 天，模板：工作压力）
(7, 2, 1, '最近工作压力很大，感觉快撑不住了。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:06:00')),
(8, 2, 2, '听起来你承受了很多，愿意具体说说是什么让你感到压力吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:08:00')),
(9, 2, 1, '项目一直在赶进度，每天都要加班到很晚。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:10:00')),
(10, 2, 2, '长期的加班确实会消耗人的精力。这段时间你的休息和吃饭规律吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:12:00')),
(11, 2, 1, '不太规律，经常忘了吃饭。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:14:00')),
(12, 2, 2, '身体状态会直接影响情绪。我们先从照顾好自己开始：固定三餐、每小时起来活动五分钟，可以吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:16:00')),
-- 会话3（-4 天，模板：朋友矛盾）
(13, 3, 1, '和朋友闹矛盾了，心里一直堵得慌。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:14:00')),
(14, 3, 2, '和朋友产生矛盾确实让人难受。愿意说说发生了什么吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:16:00')),
(15, 3, 1, '一件小事，TA 当着很多人的面开了我的玩笑。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:18:00')),
(16, 3, 2, '在他人面前被调侃，感到不舒服是很正常的。你当时有表达出自己的感受吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:20:00')),
(17, 3, 1, '没有，只是笑了笑，回家越想越委屈。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:22:00')),
(18, 3, 2, '被压抑的感受不会自动消失。如果这段友谊对你重要，可以找个私下场合，平和地告诉 TA 你的感受。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 14:24:00')),
-- 会话4（-7 天，模板：持续低落）
(19, 4, 1, '感觉最近情绪一直很低落，对什么都提不起兴趣。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:31:00')),
(20, 4, 2, '谢谢你愿意说出来。这种「提不起兴趣」的状态持续多久了呢？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:33:00')),
(21, 4, 1, '差不多一个月了，以前喜欢的事情现在也不想做。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:35:00')),
(22, 4, 2, '持续一个月的低兴趣状态值得认真对待。除了兴趣下降，睡眠和食欲有变化吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:37:00')),
(23, 4, 1, '睡眠还行，就是容易累，吃得也少了。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:39:00')),
(24, 4, 2, '如果这种状态持续加重，建议寻求专业心理帮助，这不是软弱，而是对自己负责。同时我们可以先从每天一件小事开始。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), ' 21:41:00')),
-- 会话5（-12 天，模板：面试紧张）
(25, 5, 1, '明天要面试了，特别紧张，怕又搞砸。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:48:00')),
(26, 5, 2, '面试前的紧张说明你在意这次机会。愿意说说上次让你觉得「搞砸」的地方吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:50:00')),
(27, 5, 1, '上次被问到职业规划时脑子一片空白。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:52:00')),
(28, 5, 2, '这属于常见的应激反应。我们可以提前准备一个 30 秒的简短回答，多练习几遍，紧张会小很多。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:54:00')),
(29, 5, 1, '好主意，我晚上就写一版。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:56:00')),
(30, 5, 2, '另外记住：面试是双向选择，你不是在接受审判。祝你明天顺利。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 12 DAY), ' 10:58:00')),
-- 会话6（-18 天，模板：睡眠）
(31, 6, 1, '最近总是睡不好，躺在床上翻来覆去一两个小时都睡不着。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:21:00')),
(32, 6, 2, '你好，愿意和我多聊聊最近的睡眠情况吗？大概是从什么时候开始的？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:23:00')),
(33, 6, 1, '嗯，大概两周了吧，晚上要很久才能睡着。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:25:00')),
(34, 6, 2, '两周里有没有哪一晚相对好一些？当时和平时有什么不一样吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:27:00')),
(35, 6, 1, '周末运动过之后那晚睡得还行。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:29:00')),
(36, 6, 2, '这是个很好的线索。规律运动对睡眠有帮助，我们可以从每天 20 分钟的散步开始试试。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 18 DAY), ' 19:31:00')),
-- 会话7（-23 天，模板：工作压力）
(37, 7, 1, '最近工作压力很大，感觉快撑不住了。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:04:00')),
(38, 7, 2, '听起来你承受了很多，愿意具体说说是什么让你感到压力吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:06:00')),
(39, 7, 1, '项目一直在赶进度，每天都要加班到很晚。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:08:00')),
(40, 7, 2, '长期的加班确实会消耗人的精力。这段时间你的休息和吃饭规律吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:10:00')),
(41, 7, 1, '不太规律，经常忘了吃饭。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:12:00')),
(42, 7, 2, '身体状态会直接影响情绪。我们先从照顾好自己开始：固定三餐、每小时起来活动五分钟，可以吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 23 DAY), ' 15:14:00')),
-- 会话8（-29 天，模板：持续低落）
(43, 8, 1, '感觉最近情绪一直很低落，对什么都提不起兴趣。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:12:00')),
(44, 8, 2, '谢谢你愿意说出来。这种「提不起兴趣」的状态持续多久了呢？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:14:00')),
(45, 8, 1, '差不多一个月了，以前喜欢的事情现在也不想做。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:16:00')),
(46, 8, 2, '持续一个月的低兴趣状态值得认真对待。除了兴趣下降，睡眠和食欲有变化吗？', CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:18:00')),
(47, 8, 1, '睡眠还行，就是容易累，吃得也少了。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:20:00')),
(48, 8, 2, '如果这种状态持续加重，建议寻求专业心理帮助，这不是软弱，而是对自己负责。同时我们可以先从每天一件小事开始。', CONCAT(DATE_SUB(CURDATE(), INTERVAL 29 DAY), ' 20:22:00'));
