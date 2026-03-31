package com.suncan.english.module.progress.service;

import com.suncan.english.module.progress.vo.CorrectRateTrendVO;
import com.suncan.english.module.progress.vo.ProgressCalendarVO;

public interface ProgressService {

    ProgressCalendarVO getCalendar(Long userId, Integer year, Integer month);

    CorrectRateTrendVO getCorrectRateTrend(Long userId);
}

