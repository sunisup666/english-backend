package com.suncan.english.module.reward.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.module.reward.entity.Badge;
import com.suncan.english.module.user.entity.User;
import com.suncan.english.module.reward.entity.UserBadge;
import com.suncan.english.module.reward.entity.UserPoints;
import com.suncan.english.module.practice.entity.UserPracticeRecord;
import com.suncan.english.shared.enums.BadgeConditionTypeEnum;
import com.suncan.english.module.reward.mapper.BadgeMapper;
import com.suncan.english.module.reward.mapper.UserBadgeMapper;
import com.suncan.english.module.user.mapper.UserMapper;
import com.suncan.english.module.reward.mapper.UserPointsMapper;
import com.suncan.english.module.practice.mapper.UserPracticeRecordMapper;
import com.suncan.english.module.reward.service.BadgeService;
import com.suncan.english.module.reward.vo.BadgeVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BadgeServiceImpl implements BadgeService {

    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;
    private final UserPracticeRecordMapper userPracticeRecordMapper;
    private final UserPointsMapper userPointsMapper;
    private final UserMapper userMapper;

    public BadgeServiceImpl(BadgeMapper badgeMapper,
                            UserBadgeMapper userBadgeMapper,
                            UserPracticeRecordMapper userPracticeRecordMapper,
                            UserPointsMapper userPointsMapper,
                            UserMapper userMapper) {
        this.badgeMapper = badgeMapper;
        this.userBadgeMapper = userBadgeMapper;
        this.userPracticeRecordMapper = userPracticeRecordMapper;
        this.userPointsMapper = userPointsMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<BadgeVO> listBadges(Long userId) {
        List<Badge> badgeList = badgeMapper.selectList(
                new LambdaQueryWrapper<Badge>()
                        .eq(Badge::getStatus, 1)
                        .orderByAsc(Badge::getConditionType, Badge::getConditionValue, Badge::getId)
        );
        if (badgeList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, UserBadge> earnedBadgeMap = loadEarnedBadgeMap(userId);
        List<BadgeVO> result = new ArrayList<>();
        for (Badge badge : badgeList) {
            UserBadge userBadge = earnedBadgeMap.get(badge.getId());
            result.add(toBadgeVO(badge, userBadge));
        }
        return result;
    }

    @Override
    public List<BadgeVO> checkAndGrantBadges(Long userId) {
        List<Badge> badgeList = badgeMapper.selectList(
                new LambdaQueryWrapper<Badge>()
                        .eq(Badge::getStatus, 1)
                        .orderByAsc(Badge::getConditionType, Badge::getConditionValue, Badge::getId)
        );
        if (badgeList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, UserBadge> earnedBadgeMap = loadEarnedBadgeMap(userId);
        int continuousStudyDays = queryContinuousStudyDays(userId);
        long totalPracticeCount = userPracticeRecordMapper.selectCount(
                new LambdaQueryWrapper<UserPracticeRecord>()
                        .eq(UserPracticeRecord::getUserId, userId)
        );
        int totalPoints = queryTotalPoints(userId);
        int englishLevel = queryEnglishLevel(userId);

        LocalDateTime now = LocalDateTime.now();
        List<BadgeVO> grantedList = new ArrayList<>();
        for (Badge badge : badgeList) {
            if (earnedBadgeMap.containsKey(badge.getId())) {
                continue;
            }
            if (!isBadgeSatisfied(badge, continuousStudyDays, totalPracticeCount, totalPoints, englishLevel)) {
                continue;
            }

            UserBadge userBadge = new UserBadge();
            userBadge.setUserId(userId);
            userBadge.setBadgeId(badge.getId());
            userBadge.setEarnedTime(now);
            userBadgeMapper.insert(userBadge);
            grantedList.add(toBadgeVO(badge, userBadge));
        }
        return grantedList;
    }

    private Map<Long, UserBadge> loadEarnedBadgeMap(Long userId) {
        List<UserBadge> earnedList = userBadgeMapper.selectList(
                new LambdaQueryWrapper<UserBadge>()
                        .eq(UserBadge::getUserId, userId)
        );
        if (earnedList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, UserBadge> result = new HashMap<>();
        for (UserBadge userBadge : earnedList) {
            result.put(userBadge.getBadgeId(), userBadge);
        }
        return result;
    }

    private boolean isBadgeSatisfied(Badge badge,
                                     int continuousStudyDays,
                                     long totalPracticeCount,
                                     int totalPoints,
                                     int englishLevel) {
        Integer conditionType = badge.getConditionType();
        Integer conditionValue = badge.getConditionValue();
        if (conditionType == null || conditionValue == null) {
            return false;
        }

        if (BadgeConditionTypeEnum.CONTINUOUS_STUDY_DAYS.getCode().equals(conditionType)) {
            return continuousStudyDays >= conditionValue;
        }
        if (BadgeConditionTypeEnum.TOTAL_PRACTICE_COUNT.getCode().equals(conditionType)) {
            return totalPracticeCount >= conditionValue;
        }
        if (BadgeConditionTypeEnum.TOTAL_POINTS.getCode().equals(conditionType)) {
            return totalPoints >= conditionValue;
        }
        if (BadgeConditionTypeEnum.LEVEL_REACHED.getCode().equals(conditionType)) {
            return englishLevel >= conditionValue;
        }
        return false;
    }

    private int queryContinuousStudyDays(Long userId) {
        List<Object> dateList = userPracticeRecordMapper.selectObjs(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserPracticeRecord>()
                        .select("DATE(submit_time)")
                        .eq("user_id", userId)
                        .isNotNull("submit_time")
                        .groupBy("DATE(submit_time)")
                        .orderByDesc("DATE(submit_time)")
        );
        if (dateList.isEmpty()) {
            return 0;
        }

        Set<LocalDate> studyDates = new HashSet<>();
        for (Object item : dateList) {
            LocalDate date = toLocalDate(item);
            if (date != null) {
                studyDates.add(date);
            }
        }

        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (studyDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private int queryTotalPoints(Long userId) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, userId)
                        .last("limit 1")
        );
        return userPoints == null || userPoints.getTotalPoints() == null ? 0 : userPoints.getTotalPoints();
    }

    private int queryEnglishLevel(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null || user.getEnglishLevel() == null ? 0 : user.getEnglishLevel();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private BadgeVO toBadgeVO(Badge badge, UserBadge userBadge) {
        BadgeVO vo = new BadgeVO();
        vo.setBadgeId(badge.getId());
        vo.setBadgeCode(badge.getBadgeCode());
        vo.setBadgeName(badge.getBadgeName());
        vo.setDescription(badge.getDescription());
        vo.setIconUrl(badge.getIconUrl());
        vo.setConditionType(badge.getConditionType());
        vo.setConditionTypeName(BadgeConditionTypeEnum.getNameByCode(badge.getConditionType()));
        vo.setConditionValue(badge.getConditionValue());
        vo.setIsEarned(userBadge == null ? 0 : 1);
        vo.setEarnedTime(userBadge == null ? null : userBadge.getEarnedTime());
        return vo;
    }
}


