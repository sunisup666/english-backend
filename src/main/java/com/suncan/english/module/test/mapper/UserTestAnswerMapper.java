package com.suncan.english.module.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.module.test.entity.UserTestAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户答题明细 Mapper。
 */
@Mapper
public interface UserTestAnswerMapper extends BaseMapper<UserTestAnswer> {
}
