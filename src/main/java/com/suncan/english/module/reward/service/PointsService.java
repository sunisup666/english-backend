package com.suncan.english.module.reward.service;

public interface PointsService {

    void addPoints(Long userId, int points);

    int getTotalPoints(Long userId);
}

