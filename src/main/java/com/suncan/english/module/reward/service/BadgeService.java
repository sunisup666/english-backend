package com.suncan.english.module.reward.service;

import com.suncan.english.module.reward.vo.BadgeVO;

import java.util.List;

public interface BadgeService {

    List<BadgeVO> listBadges(Long userId);

    List<BadgeVO> checkAndGrantBadges(Long userId);
}

