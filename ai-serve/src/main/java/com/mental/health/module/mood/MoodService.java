package com.mental.health.module.mood;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.common.PageResult;
import com.mental.health.module.mood.dto.MoodCreateRequest;
import com.mental.health.module.mood.vo.EmotionGardenVO;
import com.mental.health.module.mood.vo.MoodLogVO;
import com.mental.health.module.user.User;
import com.mental.health.module.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 情绪日志服务：管理端查看 + 用户端记录/列表 + 情绪花园
 */
@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodLogMapper moodLogMapper;
    private final UserMapper userMapper;

    // ---------- 用户端 ----------

    /** 记录心情：score 1-10（越界 1204）、tags ≤2 且白名单（1203）、content ≤200（DTO 校验）；同一天可多条 */
    public MoodLogVO create(Long uid, MoodCreateRequest request) {
        if (request.getScore() < 1 || request.getScore() > 10) {
            throw new BusinessException(ErrorCode.SCORE_OUT_OF_RANGE);
        }
        MoodTags.check(request.getTags());

        MoodLog log = new MoodLog();
        log.setUserId(uid);
        log.setScore(request.getScore());
        log.setTags(request.getTags() == null || request.getTags().isEmpty()
                ? null : String.join(",", request.getTags()));
        log.setContent(request.getContent());
        moodLogMapper.insert(log);

        return toVO(log, loadNicknames(List.of(uid)).get(uid));
    }

    /** 我的记录：仅本人、时间倒序、分页 */
    public PageResult<MoodLogVO> pageForUser(Long uid, long page, long pageSize) {
        Page<MoodLog> result = moodLogMapper.selectPage(Page.of(page, pageSize),
                new LambdaQueryWrapper<MoodLog>()
                        .eq(MoodLog::getUserId, uid)
                        .orderByDesc(MoodLog::getCreatedAt)
                        .orderByDesc(MoodLog::getId));
        Map<Long, String> nicknames = loadNicknames(List.of(uid));
        return PageResult.of(result.convert(m -> toVO(m, nicknames.get(m.getUserId()))));
    }

    /** 情绪花园：今日最新一条；无则 null。score≤3 低落 / 4-6 中性 / ≥7 不错 */
    public EmotionGardenVO emotionGarden(Long uid) {
        MoodLog latest = moodLogMapper.selectList(new LambdaQueryWrapper<MoodLog>()
                        .eq(MoodLog::getUserId, uid)
                        .ge(MoodLog::getCreatedAt, LocalDate.now().atStartOfDay())
                        .orderByDesc(MoodLog::getCreatedAt)
                        .orderByDesc(MoodLog::getId)
                        .last("LIMIT 1"))
                .stream().findFirst().orElse(null);
        if (latest == null) {
            return null;
        }
        int score = latest.getScore();
        String state = score <= 3 ? "低落" : score <= 6 ? "中性" : "不错";
        String color = score <= 3 ? "#F56C6C" : score <= 6 ? "#F0A8B8" : "#8FD3A8";
        String tip = score <= 3 ? "给自己一个拥抱，试试缓慢的深呼吸"
                : score <= 6 ? "情绪状态平稳，保持此刻的节奏"
                : "情绪状态良好，把今天的美好记录下来";
        return new EmotionGardenVO(score, score * 10, state, color, tip, latest.getCreatedAt());
    }

    // ---------- 管理端 ----------

    /** 分页列表：user 昵称/ID 匹配、日期范围、scoreRange 分档，时间倒序 */
    public PageResult<MoodLogVO> pageForAdmin(String user, LocalDate startDate, LocalDate endDate,
                                              String scoreRange, long page, long pageSize) {
        LambdaQueryWrapper<MoodLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(user)) {
            wrapper.in(MoodLog::getUserId, matchUserIds(user.trim()));
        }
        if (startDate != null) {
            wrapper.ge(MoodLog::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.lt(MoodLog::getCreatedAt, endDate.plusDays(1).atStartOfDay());
        }
        if (StringUtils.hasText(scoreRange)) {
            switch (scoreRange) {
                case "low" -> wrapper.between(MoodLog::getScore, 1, 3);
                case "mid" -> wrapper.between(MoodLog::getScore, 4, 6);
                case "high" -> wrapper.between(MoodLog::getScore, 7, 10);
                default -> throw new BusinessException(ErrorCode.PARAM_ERROR, "scoreRange 仅支持 low / mid / high");
            }
        }
        wrapper.orderByDesc(MoodLog::getCreatedAt).orderByDesc(MoodLog::getId);
        Page<MoodLog> result = moodLogMapper.selectPage(Page.of(page, pageSize), wrapper);
        List<Long> userIds = result.getRecords().stream().map(MoodLog::getUserId).distinct().toList();
        Map<Long, String> nicknames = loadNicknames(userIds);
        return PageResult.of(result.convert(m -> toVO(m, nicknames.get(m.getUserId()))));
    }

    /** 详情（敏感数据，调用方负责审计日志） */
    public MoodLogVO detail(Long id) {
        MoodLog log = moodLogMapper.selectById(id);
        if (log == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "情绪日志不存在");
        }
        return toVO(log, loadNicknames(List.of(log.getUserId())).get(log.getUserId()));
    }

    // ---------- 私有工具 ----------

    private List<Long> matchUserIds(String keyword) {
        List<Long> ids = new java.util.ArrayList<>();
        if (keyword.matches("\\d+")) {
            ids.add(Long.parseLong(keyword));
        }
        userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getNickname, keyword))
                .forEach(u -> ids.add(u.getId()));
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    private Map<Long, String> loadNicknames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
    }

    private MoodLogVO toVO(MoodLog log, String nickname) {
        MoodLogVO vo = new MoodLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setUserNickname(nickname);
        vo.setTime(log.getCreatedAt());
        vo.setScore(log.getScore());
        vo.setTags(log.getTags() == null || log.getTags().isBlank()
                ? List.of() : List.of(log.getTags().split(",")));
        vo.setContent(log.getContent() == null ? "" : log.getContent());
        return vo;
    }
}
