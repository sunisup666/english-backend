package com.suncan.english.module.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.module.practice.entity.UserPracticeAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习任务练习作答明细数据访问层。
 */
@Mapper
public interface UserPracticeAnswerMapper extends BaseMapper<UserPracticeAnswer> {
}