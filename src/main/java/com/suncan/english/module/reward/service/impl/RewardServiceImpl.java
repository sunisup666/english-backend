package com.suncan.english.module.reward.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.module.user.entity.User;
import com.suncan.english.module.reward.entity.UserPoints;
import com.suncan.english.module.user.mapper.UserMapper;
import com.suncan.english.module.reward.mapper.UserPointsMapper;
import com.suncan.english.module.reward.service.BadgeService;
import com.suncan.english.module.reward.service.PointsService;
import com.suncan.english.module.reward.service.RewardService;
import com.suncan.english.module.reward.vo.BadgeVO;
import com.suncan.english.module.reward.vo.PointsVO;
import com.suncan.english.module.reward.vo.RankItemVO;
import com.suncan.english.module.reward.vo.RankVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardServiceImpl implements RewardService {

    private static final int TOP_RANK_LIMIT = 20;

    private final PointsService pointsService;
    private final BadgeService badgeService;
    private final UserPointsMapper userPointsMapper;
    private final UserMapper userMapper;

    public RewardServiceImpl(PointsService pointsService,
                             BadgeService badgeService,
                             UserPointsMapper userPointsMapper,
                             UserMapper userMapper) {
        this.pointsService = pointsService;
        this.badgeService = badgeService;
        this.userPointsMapper = userPointsMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PointsVO getPoints(Long userId) {
        PointsVO vo = new PointsVO();
        vo.setTotalPoints(pointsService.getTotalPoints(userId));
        return vo;
    }

    @Override
    public RankVO getRank(Long userId) {
        List<UserPoints> topList = userPointsMapper.selectList(
                new LambdaQueryWrapper<UserPoints>()
                        .orderByDesc(UserPoints::getTotalPoints)
                        .orderByAsc(UserPoints::getUserId)
                        .last("limit " + TOP_RANK_LIMIT)
        );

        Map<Long, User> userMap = loadUserMap(topList.stream()
                .map(UserPoints::getUserId)
                .collect(Collectors.toList()));

        List<RankItemVO> rankList = new ArrayList<>();
        for (int i = 0; i < topList.size(); i++) {
            UserPoints userPoints = topList.get(i);
            RankItemVO itemVO = new RankItemVO();
            itemVO.setRank(i + 1);
            User user = userMap.get(userPoints.getUserId());
            itemVO.setNickname(user == null ? null : user.getNickname());
            itemVO.setTotalPoints(userPoints.getTotalPoints());
            rankList.add(itemVO);
        }

        UserPoints myPointsRecord = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, userId)
                        .last("limit 1")
        );

        int myPoints = myPointsRecord == null || myPointsRecord.getTotalPoints() == null
                ? 0 : myPointsRecord.getTotalPoints();
        int myRank = 0;
        if (myPointsRecord != null) {
            long higherCount = userPointsMapper.selectCount(
                    new LambdaQueryWrapper<UserPoints>()
                            .gt(UserPoints::getTotalPoints, myPoints)
            );
            long samePointsPriorCount = userPointsMapper.selectCount(
                    new LambdaQueryWrapper<UserPoints>()
                            .eq(UserPoints::getTotalPoints, myPoints)
                            .lt(UserPoints::getUserId, userId)
            );
            myRank = (int) (higherCount + samePointsPriorCount + 1);
        }

        RankVO vo = new RankVO();
        vo.setRankList(rankList);
        vo.setMyRank(myRank);
        vo.setMyPoints(myPoints);
        return vo;
    }

    @Override
    public List<BadgeVO> listBadges(Long userId) {
        return badgeService.listBadges(userId);
    }

    private Map<Long, User> loadUserMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> userList = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds)
        );
        Map<Long, User> result = new HashMap<>();
        for (User user : userList) {
            result.put(user.getId(), user);
        }
        return result;
    }
}


