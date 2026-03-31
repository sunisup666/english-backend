package com.suncan.english.module.progress.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suncan.english.module.practice.mapper.UserPracticeRecordMapper;
import com.suncan.english.module.progress.service.ProgressService;
import com.suncan.english.module.progress.vo.CorrectRateTrendVO;
import com.suncan.english.module.progress.vo.ProgressCalendarVO;
import com.suncan.english.module.progress.vo.TaskTypeWeekCorrectRateVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final UserPracticeRecordMapper userPracticeRecordMapper;

    public ProgressServiceImpl(UserPracticeRecordMapper userPracticeRecordMapper) {
        this.userPracticeRecordMapper = userPracticeRecordMapper;
    }

    @Override
    public ProgressCalendarVO getCalendar(Long userId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year == null ? now.getYear() : year;
        int targetMonth = month == null ? now.getMonthValue() : month;

        if (targetMonth < 1 || targetMonth > 12) {
            targetMonth = now.getMonthValue();
        }

        YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate nextMonthFirstDay = yearMonth.plusMonths(1).atDay(1);

        QueryWrapper<com.suncan.english.module.practice.entity.UserPracticeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(submit_time)")
                .eq("user_id", userId)
                .isNotNull("submit_time")
                .ge("submit_time", firstDay.atStartOfDay())
                .lt("submit_time", nextMonthFirstDay.atStartOfDay())
                .groupBy("DATE(submit_time)")
                .orderByAsc("DATE(submit_time)");

        List<Object> dateList = userPracticeRecordMapper.selectObjs(queryWrapper);
        List<LocalDate> studyDates = new ArrayList<>();
        for (Object item : dateList) {
            LocalDate date = toLocalDate(item);
            if (date != null) {
                studyDates.add(date);
            }
        }

        ProgressCalendarVO vo = new ProgressCalendarVO();
        vo.setYear(targetYear);
        vo.setMonth(targetMonth);
        vo.setStudyDates(studyDates);
        return vo;
    }

    @Override
    public CorrectRateTrendVO getCorrectRateTrend(Long userId) {
        LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate startWeek = currentWeekStart.minusWeeks(7);
        LocalDate endWeek = currentWeekStart.plusWeeks(1);

        List<String> weeks = buildRecentWeeks(currentWeekStart);
        List<TaskTypeWeekCorrectRateVO> aggregateList = userPracticeRecordMapper.queryWeekCorrectRateTrend(
                userId,
                startWeek.atStartOfDay(),
                endWeek.atStartOfDay()
        );

        Map<Integer, Map<String, BigDecimal>> dataMap = new LinkedHashMap<>();
        for (TaskTypeWeekCorrectRateVO item : aggregateList) {
            dataMap.computeIfAbsent(item.getTaskType(), key -> new LinkedHashMap<>())
                    .put(item.getWeekLabel(), item.getCorrectRate());
        }

        CorrectRateTrendVO vo = new CorrectRateTrendVO();
        vo.setWeeks(weeks);
        vo.setVocabulary(buildTrendValues(weeks, dataMap.get(1)));
        vo.setGrammar(buildTrendValues(weeks, dataMap.get(2)));
        vo.setListening(buildTrendValues(weeks, dataMap.get(3)));
        vo.setSpeaking(buildTrendValues(weeks, dataMap.get(4)));
        return vo;
    }

    private List<String> buildRecentWeeks(LocalDate currentWeekStart) {
        List<String> weeks = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            LocalDate date = currentWeekStart.minusWeeks(i);
            int weekBasedYear = date.get(IsoFields.WEEK_BASED_YEAR);
            int weekOfYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            weeks.add(String.format("%d-W%02d", weekBasedYear, weekOfYear));
        }
        return weeks;
    }

    private List<BigDecimal> buildTrendValues(List<String> weeks, Map<String, BigDecimal> valueMap) {
        if (weeks.isEmpty()) {
            return Collections.emptyList();
        }
        List<BigDecimal> result = new ArrayList<>();
        for (String week : weeks) {
            result.add(valueMap == null ? null : valueMap.get(week));
        }
        return result;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
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
}


