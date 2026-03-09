package com.suncan.english.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.entity.PaperQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷题目关联 Mapper。
 */
@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {
}

