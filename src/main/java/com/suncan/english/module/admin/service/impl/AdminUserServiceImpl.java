package com.suncan.english.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.module.admin.dto.AdminUserPageQueryDTO;
import com.suncan.english.module.admin.dto.AdminUserPracticeRecordQueryDTO;
import com.suncan.english.module.admin.service.AdminUserService;
import com.suncan.english.module.admin.vo.AdminUserDetailVO;
import com.suncan.english.module.admin.vo.AdminUserPageItemVO;
import com.suncan.english.module.admin.vo.AdminUserPageVO;
import com.suncan.english.module.learning.entity.StudyTask;
import com.suncan.english.module.learning.mapper.StudyTaskMapper;
import com.suncan.english.module.learning.service.StudyPlanService;
import com.suncan.english.module.learning.vo.StudyPlanVO;
import com.suncan.english.module.practice.entity.UserPracticeRecord;
import com.suncan.english.module.practice.mapper.UserPracticeRecordMapper;
import com.suncan.english.module.practice.vo.PracticeRecordItemVO;
import com.suncan.english.module.practice.vo.PracticeRecordPageVO;
import com.suncan.english.module.reward.entity.UserBadge;
import com.suncan.english.module.reward.mapper.UserBadgeMapper;
import com.suncan.english.module.reward.service.RewardService;
import com.suncan.english.module.reward.vo.PointsVO;
import com.suncan.english.module.test.dto.TestRecordQueryDTO;
import com.suncan.english.module.test.entity.UserTestRecord;
import com.suncan.english.module.test.mapper.UserTestRecordMapper;
import com.suncan.english.module.test.service.TestService;
import com.suncan.english.module.test.vo.TestRecordPageVO;
import com.suncan.english.module.test.vo.TestResultVO;
import com.suncan.english.module.user.entity.User;
import com.suncan.english.module.user.mapper.UserMapper;
import com.suncan.english.shared.enums.EnglishLevelEnum;
import com.suncan.english.shared.enums.QuestionTypeEnum;
import com.suncan.english.shared.enums.SceneTypeEnum;
import com.suncan.english.shared.enums.TaskTypeEnum;
import com.suncan.english.shared.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管理端用户管理服务实现。
 *
 * 实现说明：
 * 1. 用户分页列表与用户详情分开组装，避免后台列表页和详情页职责混淆；
 * 2. 用户学习画像在详情接口中聚合返回；
 * 3. 用户练习记录与用户测试记录分别复用练习模块和测试模块已有数据结构；
 * 4. 本类只做查看类能力，不包含启停用、删除、重置密码等写操作。
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final StudyPlanService studyPlanService;
    private final StudyTaskMapper studyTaskMapper;
    private final UserPracticeRecordMapper userPracticeRecordMapper;
    private final UserTestRecordMapper userTestRecordMapper;
    private final RewardService rewardService;
    private final UserBadgeMapper userBadgeMapper;
    private final TestService testService;

    public AdminUserServiceImpl(UserMapper userMapper,
                                StudyPlanService studyPlanService,
                                StudyTaskMapper studyTaskMapper,
                                UserPracticeRecordMapper userPracticeRecordMapper,
                                UserTestRecordMapper userTestRecordMapper,
                                RewardService rewardService,
                                UserBadgeMapper userBadgeMapper,
                                TestService testService) {
        this.userMapper = userMapper;
        this.studyPlanService = studyPlanService;
        this.studyTaskMapper = studyTaskMapper;
        this.userPracticeRecordMapper = userPracticeRecordMapper;
        this.userTestRecordMapper = userTestRecordMapper;
        this.rewardService = rewardService;
        this.userBadgeMapper = userBadgeMapper;
        this.testService = testService;
    }

    /**
     * 查询用户分页列表。
     *
     * 列表返回基础账号信息，并补充当前学习计划名称、练习记录总数、测试记录总数，
     * 便于后台表格页快速浏览。
     */
    @Override
    public AdminUserPageVO page(AdminUserPageQueryDTO queryDTO) {
        long current = queryDTO == null || queryDTO.getCurrent() == null || queryDTO.getCurrent() < 1 ? 1 : queryDTO.getCurrent();
        long size = queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() < 1 ? 10 : Math.min(queryDTO.getSize(), 50);

        Long total = userMapper.selectCount(buildUserPageWrapper(queryDTO));
        if (total == null || total == 0L) {
            AdminUserPageVO empty = new AdminUserPageVO();
            empty.setCurrent(current);
            empty.setSize(size);
            empty.setTotal(0L);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        long offset = (current - 1) * size;
        List<User> userList = userMapper.selectList(
                buildUserPageWrapper(queryDTO)
                        .orderByDesc(User::getCreateTime, User::getId)
                        .last("limit " + offset + "," + size)
        );

        List<AdminUserPageItemVO> records = new ArrayList<>();
        for (User user : userList) {
            AdminUserPageItemVO itemVO = new AdminUserPageItemVO();
            itemVO.setId(user.getId());
            itemVO.setUsername(user.getUsername());
            itemVO.setNickname(user.getNickname());
            itemVO.setEmail(user.getEmail());
            itemVO.setPhone(user.getPhone());
            itemVO.setEnglishLevel(user.getEnglishLevel());
            itemVO.setCreateTime(user.getCreateTime());

            // 列表页只补充轻量级学习概览，避免引入过重的详情聚合逻辑。
            StudyPlanVO currentPlan = studyPlanService.getCurrentPlan(user.getId());
            itemVO.setCurrentPlanName(currentPlan == null ? null : currentPlan.getPlanName());
            itemVO.setTotalPracticeCount(safeCount(userPracticeRecordMapper.selectCount(
                    new LambdaQueryWrapper<UserPracticeRecord>().eq(UserPracticeRecord::getUserId, user.getId())
            )));
            itemVO.setTotalTestCount(safeCount(userTestRecordMapper.selectCount(
                    new LambdaQueryWrapper<UserTestRecord>().eq(UserTestRecord::getUserId, user.getId())
            )));
            records.add(itemVO);
        }

        AdminUserPageVO pageVO = new AdminUserPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    /**
     * 查询用户详情与学习画像。
     *
     * 详情页强调单用户聚合视角，返回基础信息、当前学习计划、计划完成率、
     * 练习次数、测试次数、最近一次测试结果、积分与徽章数量。
     */
    @Override
    public AdminUserDetailVO detail(Long userId) {
        User user = requireUser(userId);
        AdminUserDetailVO vo = new AdminUserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setEnglishLevel(user.getEnglishLevel());
        vo.setEnglishLevelName(EnglishLevelEnum.getNameByCode(user.getEnglishLevel()));
        vo.setCreateTime(user.getCreateTime());

        StudyPlanVO currentPlan = studyPlanService.getCurrentPlan(userId);
        vo.setCurrentPlan(currentPlan);
        vo.setPlanCompletionRate(resolvePlanCompletionRate(currentPlan));
        vo.setTotalPracticeCount(safeCount(userPracticeRecordMapper.selectCount(
                new LambdaQueryWrapper<UserPracticeRecord>().eq(UserPracticeRecord::getUserId, userId)
        )));
        vo.setTotalTestCount(safeCount(userTestRecordMapper.selectCount(
                new LambdaQueryWrapper<UserTestRecord>().eq(UserTestRecord::getUserId, userId)
        )));
        vo.setLatestTestResult(loadLatestTestResult(userId));

        PointsVO pointsVO = rewardService.getPoints(userId);
        vo.setPoints(pointsVO == null || pointsVO.getTotalPoints() == null ? 0 : pointsVO.getTotalPoints());
        vo.setBadgeCount(safeCount(userBadgeMapper.selectCount(
                new LambdaQueryWrapper<UserBadge>().eq(UserBadge::getUserId, userId)
        )));
        return vo;
    }

    /**
     * 查询用户练习记录列表。
     *
     * 这里返回的是用户学习过程中的练习记录，不是测试记录。
     */
    @Override
    public PracticeRecordPageVO practiceRecords(Long userId, AdminUserPracticeRecordQueryDTO queryDTO) {
        requireUser(userId);
        long current = queryDTO == null || queryDTO.getCurrent() == null || queryDTO.getCurrent() < 1 ? 1 : queryDTO.getCurrent();
        long size = queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() < 1 ? 10 : Math.min(queryDTO.getSize(), 50);

        Long total = userPracticeRecordMapper.selectCount(buildPracticeRecordWrapper(userId, queryDTO));
        if (total == null || total == 0L) {
            PracticeRecordPageVO empty = new PracticeRecordPageVO();
            empty.setCurrent(current);
            empty.setSize(size);
            empty.setTotal(0L);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        long offset = (current - 1) * size;
        List<UserPracticeRecord> recordList = userPracticeRecordMapper.selectList(
                buildPracticeRecordWrapper(userId, queryDTO)
                        .orderByDesc(UserPracticeRecord::getSubmitTime, UserPracticeRecord::getId)
                        .last("limit " + offset + "," + size)
        );

        List<PracticeRecordItemVO> records = new ArrayList<>();
        for (UserPracticeRecord record : recordList) {
            PracticeRecordItemVO itemVO = new PracticeRecordItemVO();
            itemVO.setRecordId(record.getId());
            itemVO.setPlanId(record.getPlanId());
            itemVO.setTaskId(record.getTaskId());
            itemVO.setTaskType(record.getTaskType());
            itemVO.setTaskTypeName(TaskTypeEnum.getNameByCode(record.getTaskType()));
            itemVO.setQuestionType(record.getQuestionType());
            itemVO.setQuestionTypeName(QuestionTypeEnum.getNameByCode(record.getQuestionType()));
            itemVO.setSceneType(record.getSceneType());
            itemVO.setSceneTypeName(SceneTypeEnum.getNameByCode(record.getSceneType()));
            itemVO.setTotalCount(record.getTotalCount());
            itemVO.setCorrectCount(record.getCorrectCount());
            itemVO.setTotalScore(record.getTotalScore());
            itemVO.setDurationSeconds(record.getDurationSeconds());
            itemVO.setSubmitTime(record.getSubmitTime());
            records.add(itemVO);
        }

        PracticeRecordPageVO pageVO = new PracticeRecordPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    /**
     * 查询用户测试记录列表。
     *
     * 这里复用测试模块现有分页查询能力，保持后台与用户端测试记录字段风格一致。
     */
    @Override
    public TestRecordPageVO testRecords(Long userId, TestRecordQueryDTO queryDTO) {
        requireUser(userId);
        return testService.queryRecordPage(userId, queryDTO == null ? new TestRecordQueryDTO() : queryDTO);
    }

    /**
     * 构建用户分页查询条件。
     */
    private LambdaQueryWrapper<User> buildUserPageWrapper(AdminUserPageQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO == null) {
            return wrapper;
        }
        String keyword = trimToNull(queryDTO.getKeyword());
        wrapper.eq(queryDTO.getEnglishLevel() != null, User::getEnglishLevel, queryDTO.getEnglishLevel())
                .and(keyword != null, w -> w.like(User::getUsername, keyword)
                        .or().like(User::getNickname, keyword)
                        .or().like(User::getPhone, keyword)
                        .or().like(User::getEmail, keyword));
        return wrapper;
    }

    /**
     * 构建用户练习记录查询条件。
     */
    private LambdaQueryWrapper<UserPracticeRecord> buildPracticeRecordWrapper(Long userId, AdminUserPracticeRecordQueryDTO queryDTO) {
        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<UserPracticeRecord>()
                .eq(UserPracticeRecord::getUserId, userId)
                .eq(queryDTO != null && queryDTO.getTaskType() != null, UserPracticeRecord::getTaskType, queryDTO.getTaskType());
        if (queryDTO != null && queryDTO.getStartDate() != null) {
            wrapper.ge(UserPracticeRecord::getSubmitTime, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO != null && queryDTO.getEndDate() != null) {
            LocalDate endDate = queryDTO.getEndDate();
            wrapper.lt(UserPracticeRecord::getSubmitTime, endDate.plusDays(1).atStartOfDay());
        }
        return wrapper;
    }

    /**
     * 计算当前学习计划完成率。
     */
    private BigDecimal resolvePlanCompletionRate(StudyPlanVO currentPlan) {
        if (currentPlan == null || currentPlan.getId() == null) {
            return BigDecimal.ZERO;
        }
        Long totalTask = studyTaskMapper.selectCount(new LambdaQueryWrapper<StudyTask>().eq(StudyTask::getPlanId, currentPlan.getId()));
        if (totalTask == null || totalTask <= 0) {
            return BigDecimal.ZERO;
        }
        Long completedTask = studyTaskMapper.selectCount(
                new LambdaQueryWrapper<StudyTask>()
                        .eq(StudyTask::getPlanId, currentPlan.getId())
                        .eq(StudyTask::getStatus, 1)
        );
        long completed = completedTask == null ? 0L : completedTask;
        return BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(totalTask), 4, RoundingMode.HALF_UP);
    }

    /**
     * 查询最近一次测试结果。
     */
    private TestResultVO loadLatestTestResult(Long userId) {
        UserTestRecord latestRecord = userTestRecordMapper.selectOne(
                new LambdaQueryWrapper<UserTestRecord>()
                        .eq(UserTestRecord::getUserId, userId)
                        .orderByDesc(UserTestRecord::getSubmitTime)
                        .orderByDesc(UserTestRecord::getId)
                        .last("limit 1")
        );
        if (latestRecord == null) {
            return null;
        }
        TestResultVO vo = new TestResultVO();
        vo.setRecordId(latestRecord.getId());
        vo.setPaperId(latestRecord.getPaperId());
        vo.setTotalScore(latestRecord.getTotalScore());
        vo.setCorrectCount(latestRecord.getCorrectCount());
        vo.setLevelResult(latestRecord.getLevelResult());
        vo.setLevelResultName(EnglishLevelEnum.getNameByCode(latestRecord.getLevelResult()));
        vo.setStartTime(latestRecord.getStartTime());
        vo.setSubmitTime(latestRecord.getSubmitTime());
        vo.setDurationSeconds(latestRecord.getDurationSeconds());
        return vo;
    }

    /**
     * 校验用户是否存在。
     */
    private User requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 处理 count 结果为空的情况。
     */
    private Long safeCount(Long count) {
        return count == null ? 0L : count;
    }

    /**
     * 去除空白并将空字符串转为 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}