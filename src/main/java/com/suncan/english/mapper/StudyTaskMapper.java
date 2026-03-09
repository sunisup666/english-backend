package com.suncan.english.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.entity.StudyTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习任务数据访问层。
 */
@Mapper
public interface StudyTaskMapper extends BaseMapper<StudyTask> {
}
