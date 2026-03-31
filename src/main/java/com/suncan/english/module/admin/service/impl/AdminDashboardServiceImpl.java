package com.suncan.english.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.module.admin.service.AdminDashboardService;
import com.suncan.english.module.admin.vo.AdminDashboardVO;
import com.suncan.english.module.learning.entity.StudyPlan;
import com.suncan.english.module.learning.mapper.StudyPlanMapper;
import com.suncan.english.module.practice.entity.UserPracticeRecord;
import com.suncan.english.module.practice.mapper.UserPracticeRecordMapper;
import com.suncan.english.module.questionbank.entity.Question;
import com.suncan.english.module.questionbank.entity.TestPaper;
import com.suncan.english.module.questionbank.mapper.QuestionMapper;
import com.suncan.english.module.questionbank.mapper.TestPaperMapper;
import com.suncan.english.module.test.entity.UserTestRecord;
import com.suncan.english.module.test.mapper.UserTestRecordMapper;
import com.suncan.english.module.user.entity.User;
import com.suncan.english.module.user.mapper.UserMapper;
import com.suncan.english.shared.enums.StudyPlanStatusEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final TestPaperMapper testPaperMapper;
    private final UserPracticeRecordMapper userPracticeRecordMapper;
    private final UserTestRecordMapper userTestRecordMapper;
    private final StudyPlanMapper studyPlanMapper;

    public AdminDashboardServiceImpl(UserMapper userMapper,
                                     QuestionMapper questionMapper,
                                     TestPaperMapper testPaperMapper,
                                     UserPracticeRecordMapper userPracticeRecordMapper,
                                     UserTestRecordMapper userTestRecordMapper,
                                     StudyPlanMapper studyPlanMapper) {
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.testPaperMapper = testPaperMapper;
        this.userPracticeRecordMapper = userPracticeRecordMapper;
        this.userTestRecordMapper = userTestRecordMapper;
        this.studyPlanMapper = studyPlanMapper;
    }

    @Override
    public AdminDashboardVO getDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();
        vo.setUserCount(safeCount(userMapper.selectCount(null)));
        vo.setQuestionCount(safeCount(questionMapper.selectCount(null)));
        vo.setPaperCount(safeCount(testPaperMapper.selectCount(null)));
        vo.setPracticeRecordCount(safeCount(userPracticeRecordMapper.selectCount(null)));
        vo.setTestRecordCount(safeCount(userTestRecordMapper.selectCount(null)));
        vo.setActivePlanCount(safeCount(
                studyPlanMapper.selectCount(
                        new LambdaQueryWrapper<StudyPlan>()
                                .eq(StudyPlan::getStatus, StudyPlanStatusEnum.RUNNING.getCode())
                )
        ));

        LocalDate today = LocalDate.now();
        vo.setTodayNewUserCount(safeCount(
                userMapper.selectCount(
                        new LambdaQueryWrapper<User>()
                                .ge(User::getCreateTime, today.atStartOfDay())
                                .lt(User::getCreateTime, today.plusDays(1).atStartOfDay())
                )
        ));
        return vo;
    }

    private Long safeCount(Long count) {
        return count == null ? 0L : count;
    }
}