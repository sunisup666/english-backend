package com.suncan.english.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.entity.UserPracticeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习任务练习记录数据访问层。
 */
@Mapper
public interface UserPracticeRecordMapper extends BaseMapper<UserPracticeRecord> {
}
