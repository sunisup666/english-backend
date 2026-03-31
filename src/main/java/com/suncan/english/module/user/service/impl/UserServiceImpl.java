package com.suncan.english.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suncan.english.module.learning.entity.StudyTask;
import com.suncan.english.module.learning.mapper.StudyTaskMapper;
import com.suncan.english.module.learning.service.StudyPlanService;
import com.suncan.english.module.learning.vo.StudyPlanVO;
import com.suncan.english.module.practice.entity.UserPracticeRecord;
import com.suncan.english.module.practice.mapper.UserPracticeRecordMapper;
import com.suncan.english.module.test.entity.UserTestRecord;
import com.suncan.english.module.test.mapper.UserTestRecordMapper;
import com.suncan.english.module.test.vo.TestResultVO;
import com.suncan.english.module.user.dto.LoginDTO;
import com.suncan.english.module.user.dto.RegisterDTO;
import com.suncan.english.module.user.dto.UpdatePasswordDTO;
import com.suncan.english.module.user.dto.UpdateUserDTO;
import com.suncan.english.module.user.entity.User;
import com.suncan.english.module.user.mapper.UserMapper;
import com.suncan.english.module.user.service.UserService;
import com.suncan.english.module.user.vo.UserDashboardVO;
import com.suncan.english.module.user.vo.UserInfoVO;
import com.suncan.english.shared.enums.EnglishLevelEnum;
import com.suncan.english.shared.enums.TaskTypeEnum;
import com.suncan.english.shared.exception.BusinessException;
import com.suncan.english.shared.util.Md5Util;
import com.suncan.english.shared.util.TokenUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final int TASK_STATUS_COMPLETED = 1;

    private final TokenUtil tokenUtil;
    private final StudyPlanService studyPlanService;
    private final StudyTaskMapper studyTaskMapper;
    private final UserPracticeRecordMapper userPracticeRecordMapper;
    private final UserTestRecordMapper userTestRecordMapper;

    public UserServiceImpl(
            TokenUtil tokenUtil,
            StudyPlanService studyPlanService,
            StudyTaskMapper studyTaskMapper,
            UserPracticeRecordMapper userPracticeRecordMapper,
            UserTestRecordMapper userTestRecordMapper
    ) {
        this.tokenUtil = tokenUtil;
        this.studyPlanService = studyPlanService;
        this.studyTaskMapper = studyTaskMapper;
        this.userPracticeRecordMapper = userPracticeRecordMapper;
        this.userTestRecordMapper = userTestRecordMapper;
    }

    @Override
    public void register(RegisterDTO dto) {
        String username = normalizeRequired(dto.getUsername(), "用户名不能为空");
        long duplicateCount = this.lambdaQuery().eq(User::getUsername, username).count();
        if (duplicateCount > 0) {
            throw new BusinessException("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.md5(dto.getPassword()));
        user.setNickname(normalizeOptional(dto.getNickname()));
        user.setEmail(normalizeOptional(dto.getEmail()));
        user.setPhone(normalizeOptional(dto.getPhone()));
        user.setEnglishLevel(EnglishLevelEnum.BEGINNER.getCode());
        user.setCreateTime(now);
        user.setUpdateTime(now);
        this.save(user);
    }

    @Override
    public String login(LoginDTO dto) {
        String username = normalizeRequired(dto.getUsername(), "用户名不能为空");
        User user = this.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Md5Util.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return tokenUtil.createToken(user.getId(), user.getUsername());
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setEnglishLevel(user.getEnglishLevel());
        vo.setEnglishLevelName(EnglishLevelEnum.getNameByCode(user.getEnglishLevel()));
        return vo;
    }

    public UserDashboardVO getDashboard(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserDashboardVO vo = new UserDashboardVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setEnglishLevel(user.getEnglishLevel());
        vo.setEnglishLevelName(EnglishLevelEnum.getNameByCode(user.getEnglishLevel()));

        fillPracticeStats(userId, vo);
        fillCurrentPlanStats(userId, vo);
        fillLatestTestStats(userId, vo);
        fillLevelTrend(userId, vo);
        return vo;
    }

    @Override
    public void updateUser(Long userId, UpdateUserDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String nickname = normalizeOptional(dto.getNickname());
        String email = normalizeOptional(dto.getEmail());
        String phone = normalizeOptional(dto.getPhone());
        if (nickname == null && email == null && phone == null) {
            throw new BusinessException("至少传一个要更新的字段");
        }

        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(nickname != null, User::getNickname, nickname)
                .set(email != null, User::getEmail, email)
                .set(phone != null, User::getPhone, phone)
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();
        if (!Md5Util.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (Md5Util.matches(newPassword, user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getPassword, Md5Util.md5(newPassword))
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void updateEnglishLevel(Long userId, Integer englishLevel) {
        if (!EnglishLevelEnum.containsCode(englishLevel)) {
            throw new BusinessException("英语等级编码不合法");
        }
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getEnglishLevel, englishLevel)
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
    }

    private void fillPracticeStats(Long userId, UserDashboardVO vo) {
        List<UserPracticeRecord> records = userPracticeRecordMapper.selectList(
                new LambdaQueryWrapper<UserPracticeRecord>()
                        .eq(UserPracticeRecord::getUserId, userId)
                        .orderByDesc(UserPracticeRecord::getSubmitTime, UserPracticeRecord::getId)
        );
        if (records == null || records.isEmpty()) {
            vo.setTotalPracticeCount(0L);
            vo.setTotalStudyMinutes(0L);
            vo.setTotalCorrectRate(BigDecimal.ZERO);
            vo.setPracticeByType(Collections.emptyMap());
            vo.setCorrectRateByType(Collections.emptyMap());
            vo.setContinuousStudyDays(0);
            return;
        }

        long totalPracticeCount = records.size();
        long totalDurationSeconds = 0L;
        long totalCorrectCount = 0L;
        long totalQuestionCount = 0L;
        Map<String, Long> practiceByType = new LinkedHashMap<>();
        Map<String, long[]> correctRateSummaryByType = new LinkedHashMap<>();

        for (UserPracticeRecord record : records) {
            totalDurationSeconds += safeLong(record.getDurationSeconds());
            totalCorrectCount += safeLong(record.getCorrectCount());
            totalQuestionCount += safeLong(record.getTotalCount());

            String taskTypeName = resolveTaskTypeName(record.getTaskType());
            practiceByType.merge(taskTypeName, 1L, Long::sum);

            long[] summary = correctRateSummaryByType.computeIfAbsent(taskTypeName, key -> new long[2]);
            summary[0] += safeLong(record.getCorrectCount());
            summary[1] += safeLong(record.getTotalCount());
        }

        Map<String, BigDecimal> correctRateByType = new LinkedHashMap<>();
        for (Map.Entry<String, long[]> entry : correctRateSummaryByType.entrySet()) {
            long correctCount = entry.getValue()[0];
            long totalCount = entry.getValue()[1];
            correctRateByType.put(entry.getKey(), calculateRate(correctCount, totalCount));
        }

        vo.setTotalPracticeCount(totalPracticeCount);
        vo.setTotalStudyMinutes(totalDurationSeconds / 60);
        vo.setTotalCorrectRate(calculateRate(totalCorrectCount, totalQuestionCount));
        vo.setPracticeByType(practiceByType);
        vo.setCorrectRateByType(correctRateByType);
        vo.setContinuousStudyDays(calculateContinuousStudyDays(records));
    }

    private void fillCurrentPlanStats(Long userId, UserDashboardVO vo) {
        StudyPlanVO currentPlan = studyPlanService.getCurrentPlan(userId);
        if (currentPlan == null || currentPlan.getId() == null) {
            vo.setTodayTaskTotal(0L);
            vo.setTodayCompletedTaskCount(0L);
            vo.setPlanTotalTask(0L);
            vo.setPlanCompletedTask(0L);
            vo.setPlanCompletionRate(BigDecimal.ZERO);
            return;
        }

        Long planId = currentPlan.getId();
        LocalDate today = LocalDate.now();

        Long planTotalTask = countStudyTasks(
                new LambdaQueryWrapper<StudyTask>()
                        .eq(StudyTask::getPlanId, planId)
        );
        Long planCompletedTask = countStudyTasks(
                new LambdaQueryWrapper<StudyTask>()
                        .eq(StudyTask::getPlanId, planId)
                        .eq(StudyTask::getStatus, TASK_STATUS_COMPLETED)
        );
        Long todayTaskTotal = countStudyTasks(
                new LambdaQueryWrapper<StudyTask>()
                        .eq(StudyTask::getPlanId, planId)
                        .eq(StudyTask::getTaskDate, today)
        );
        Long todayCompletedTaskCount = countStudyTasks(
                new LambdaQueryWrapper<StudyTask>()
                        .eq(StudyTask::getPlanId, planId)
                        .eq(StudyTask::getTaskDate, today)
                        .eq(StudyTask::getStatus, TASK_STATUS_COMPLETED)
        );

        vo.setPlanTotalTask(planTotalTask);
        vo.setPlanCompletedTask(planCompletedTask);
        vo.setTodayTaskTotal(todayTaskTotal);
        vo.setTodayCompletedTaskCount(todayCompletedTaskCount);
        vo.setPlanCompletionRate(calculateCompletionRate(planCompletedTask, planTotalTask));
    }

    private void fillLatestTestStats(Long userId, UserDashboardVO vo) {
        Long totalTestCount = userTestRecordMapper.selectCount(
                new LambdaQueryWrapper<UserTestRecord>()
                        .eq(UserTestRecord::getUserId, userId)
        );
        vo.setTotalTestCount(totalTestCount == null ? 0L : totalTestCount);

        UserTestRecord latestRecord = userTestRecordMapper.selectOne(
                new LambdaQueryWrapper<UserTestRecord>()
                        .eq(UserTestRecord::getUserId, userId)
                        .orderByDesc(UserTestRecord::getSubmitTime)
                        .orderByDesc(UserTestRecord::getId)
                        .last("limit 1")
        );
        vo.setLatestTestResult(toTestResultVO(latestRecord));
    }

    private void fillLevelTrend(Long userId, UserDashboardVO vo) {
        List<UserTestRecord> records = userTestRecordMapper.selectList(
                new LambdaQueryWrapper<UserTestRecord>()
                        .eq(UserTestRecord::getUserId, userId)
                        .isNotNull(UserTestRecord::getSubmitTime)
                        .orderByDesc(UserTestRecord::getSubmitTime, UserTestRecord::getId)
                        .last("limit 10")
        );
        if (records == null || records.isEmpty()) {
            vo.setLevelTrend(Collections.emptyList());
            return;
        }

        records.sort(Comparator
                .comparing(UserTestRecord::getSubmitTime)
                .thenComparing(UserTestRecord::getId));

        List<UserDashboardVO.LevelTrendItem> levelTrend = new ArrayList<>();
        for (UserTestRecord record : records) {
            UserDashboardVO.LevelTrendItem item = new UserDashboardVO.LevelTrendItem();
            item.setSubmitTime(record.getSubmitTime().toLocalDate());
            item.setLevelResult(record.getLevelResult());
            item.setLevelResultName(EnglishLevelEnum.getNameByCode(record.getLevelResult()));
            levelTrend.add(item);
        }
        vo.setLevelTrend(levelTrend);
    }

    private Long countStudyTasks(LambdaQueryWrapper<StudyTask> wrapper) {
        Long count = studyTaskMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private BigDecimal calculateCompletionRate(Long completedCount, Long totalCount) {
        if (totalCount == null || totalCount <= 0) {
            return BigDecimal.ZERO;
        }
        long completed = completedCount == null ? 0L : completedCount;
        return BigDecimal.valueOf(completed)
                .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private TestResultVO toTestResultVO(UserTestRecord record) {
        if (record == null) {
            return null;
        }
        TestResultVO vo = new TestResultVO();
        vo.setRecordId(record.getId());
        vo.setPaperId(record.getPaperId());
        vo.setTotalScore(record.getTotalScore());
        vo.setCorrectCount(record.getCorrectCount());
        vo.setLevelResult(record.getLevelResult());
        vo.setLevelResultName(EnglishLevelEnum.getNameByCode(record.getLevelResult()));
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setDurationSeconds(record.getDurationSeconds());
        return vo;
    }

    private int calculateContinuousStudyDays(List<UserPracticeRecord> records) {
        Set<LocalDate> studyDates = new HashSet<>();
        for (UserPracticeRecord record : records) {
            if (record.getSubmitTime() != null) {
                studyDates.add(record.getSubmitTime().toLocalDate());
            }
        }

        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (studyDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private String resolveTaskTypeName(Integer taskType) {
        return taskType == null ? "未知任务" : TaskTypeEnum.getNameByCode(taskType);
    }

    private long safeLong(Integer value) {
        return value == null ? 0L : value;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(message);
        }
        return trimmed;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
