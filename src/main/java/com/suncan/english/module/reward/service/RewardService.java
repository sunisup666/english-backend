package com.suncan.english.module.reward.service;

import com.suncan.english.module.reward.vo.BadgeVO;
import com.suncan.english.module.reward.vo.PointsVO;
import com.suncan.english.module.reward.vo.RankVO;

import java.util.List;

public interface RewardService {

    PointsVO getPoints(Long userId);

    RankVO getRank(Long userId);

    List<BadgeVO> listBadges(Long userId);
}

