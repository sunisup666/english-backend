package com.suncan.english.module.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suncan.english.module.test.entity.UserTestRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户测试记录 Mapper。
 */
@Mapper
public interface UserTestRecordMapper extends BaseMapper<UserTestRecord> {
}
