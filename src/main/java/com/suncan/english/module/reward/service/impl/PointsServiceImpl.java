package com.suncan.english.module.reward.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.suncan.english.module.reward.entity.UserPoints;
import com.suncan.english.module.reward.mapper.UserPointsMapper;
import com.suncan.english.module.reward.service.PointsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PointsServiceImpl implements PointsService {

    private final UserPointsMapper userPointsMapper;

    public PointsServiceImpl(UserPointsMapper userPointsMapper) {
        this.userPointsMapper = userPointsMapper;
    }

    @Override
    public void addPoints(Long userId, int points) {
        if (userId == null || points <= 0) {
            return;
        }

        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, userId)
                        .last("limit 1")
        );
        LocalDateTime now = LocalDateTime.now();
        if (userPoints == null) {
            UserPoints insertEntity = new UserPoints();
            insertEntity.setUserId(userId);
            insertEntity.setTotalPoints(points);
            insertEntity.setUpdateTime(now);
            userPointsMapper.insert(insertEntity);
            return;
        }

        userPointsMapper.update(
                null,
                new LambdaUpdateWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, userId)
                        .set(UserPoints::getTotalPoints, userPoints.getTotalPoints() + points)
                        .set(UserPoints::getUpdateTime, now)
        );
    }

    @Override
    public int getTotalPoints(Long userId) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, userId)
                        .last("limit 1")
        );
        return userPoints == null || userPoints.getTotalPoints() == null ? 0 : userPoints.getTotalPoints();
    }
}


