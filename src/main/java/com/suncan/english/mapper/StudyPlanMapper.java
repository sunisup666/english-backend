package com.suncan.english.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.entity.StudyPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习计划数据访问层。
 */
@Mapper
public interface StudyPlanMapper extends BaseMapper<StudyPlan> {
}
