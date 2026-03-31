package com.suncan.english.module.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.module.practice.entity.UserPracticeRecord;
import com.suncan.english.module.progress.vo.TaskTypeWeekCorrectRateVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserPracticeRecordMapper extends BaseMapper<UserPracticeRecord> {

    List<TaskTypeWeekCorrectRateVO> queryWeekCorrectRateTrend(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}