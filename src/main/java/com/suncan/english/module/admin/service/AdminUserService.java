package com.suncan.english.module.admin.service;

import com.suncan.english.module.admin.dto.AdminUserPageQueryDTO;
import com.suncan.english.module.admin.dto.AdminUserPracticeRecordQueryDTO;
import com.suncan.english.module.admin.vo.AdminUserDetailVO;
import com.suncan.english.module.admin.vo.AdminUserPageVO;
import com.suncan.english.module.practice.vo.PracticeRecordPageVO;
import com.suncan.english.module.test.dto.TestRecordQueryDTO;
import com.suncan.english.module.test.vo.TestRecordPageVO;

/**
 * 管理端用户管理服务接口。
 *
 * 术语约定：
 * 1. 用户分页列表：用于后台表格页，强调筛选与概览；
 * 2. 用户详情：用于后台详情页，强调基础信息与学习画像；
 * 3. 用户学习画像：指学习计划、完成率、练习次数、测试次数、积分、徽章等聚合信息；
 * 4. 用户练习记录：指日常任务练习产生的记录；
 * 5. 用户测试记录：指测试模块产生的能力评估记录。
 */
public interface AdminUserService {

    /**
     * 查询用户分页列表。
     */
    AdminUserPageVO page(AdminUserPageQueryDTO queryDTO);

    /**
     * 查询用户详情与学习画像。
     */
    AdminUserDetailVO detail(Long userId);

    /**
     * 查询用户练习记录列表。
     */
    PracticeRecordPageVO practiceRecords(Long userId, AdminUserPracticeRecordQueryDTO queryDTO);

    /**
     * 查询用户测试记录列表。
     */
    TestRecordPageVO testRecords(Long userId, TestRecordQueryDTO queryDTO);
}