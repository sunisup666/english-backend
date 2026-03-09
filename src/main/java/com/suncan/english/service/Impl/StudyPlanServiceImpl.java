package com.suncan.english.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.dto.plan.GenerateStudyPlanDTO;
import com.suncan.english.dto.plan.UpdateStudyTaskStatusDTO;
import com.suncan.english.entity.StudyPlan;
import com.suncan.english.entity.StudyTask;
import com.suncan.english.entity.User;
import com.suncan.english.enums.EnglishLevelEnum;
import com.suncan.english.enums.GoalTypeEnum;
import com.suncan.english.enums.QuestionTypeEnum;
import com.suncan.english.enums.SceneTypeEnum;
import com.suncan.english.enums.StudyPlanStatusEnum;
import com.suncan.english.enums.StudyTaskStatusEnum;
import com.suncan.english.enums.TaskTypeEnum;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.mapper.StudyPlanMapper;
import com.suncan.english.mapper.StudyTaskMapper;
import com.suncan.english.mapper.UserMapper;
import com.suncan.english.service.StudyPlanService;
import com.suncan.english.vo.plan.StudyPlanVO;
import com.suncan.english.vo.plan.StudyTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 学习计划业务实现（规则生成版）。
 *
 * 设计说明：
 * 1. 数据库仍存 Integer 编码，便于查询、统计、扩展；
 * 2. Java 业务层用枚举承载语义，避免魔法值散落；
 * 3. VO 同时返回 code + name，前端展示和联调更高效。
 */
@Service
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final int PLAN_DAYS = 7;

    private final StudyPlanMapper studyPlanMapper;
    private final StudyTaskMapper studyTaskMapper;
    private final UserMapper userMapper;

    public StudyPlanServiceImpl(StudyPlanMapper studyPlanMapper,
                                StudyTaskMapper studyTaskMapper,
                                UserMapper userMapper) {
        this.studyPlanMapper = studyPlanMapper;
        this.studyTaskMapper = studyTaskMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanVO generateStudyPlan(Long userId, GenerateStudyPlanDTO dto) {
        validateGenerateDTO(dto);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 计划生成时记录“当前等级快照”，后续可追溯计划依据。
        Integer currentLevel = normalizeLevel(user.getEnglishLevel());
        LocalDate startDate = dto.getStartDate() == null ? LocalDate.now() : dto.getStartDate();
        LocalDate endDate = startDate.plusDays(PLAN_DAYS - 1);
        LocalDateTime now = LocalDateTime.now();

        // 基础策略：一个用户同一时刻只保留一个进行中计划。
        closeRunningPlans(userId, now);

        StudyPlan plan = new StudyPlan();
        plan.setUserId(userId);
        plan.setGoalType(dto.getGoalType());
        plan.setCurrentLevel(currentLevel);
        plan.setDailyMinutes(dto.getDailyMinutes());
        plan.setPlanName(buildPlanName(dto.getGoalType(), currentLevel, dto.getDailyMinutes()));
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        // 原数字状态改为枚举，code 与数据库保持一致：PLAN_STATUS_RUNNING=1
        plan.setStatus(StudyPlanStatusEnum.RUNNING.getCode());
        plan.setCreateTime(now);
        plan.setUpdateTime(now);
        studyPlanMapper.insert(plan);

        List<StudyTask> taskList = buildPlanTasks(plan, currentLevel, now);
        for (StudyTask task : taskList) {
            studyTaskMapper.insert(task);
        }

        return toPlanVO(plan);
    }

    @Override
    public StudyPlanVO getCurrentPlan(Long userId) {
        StudyPlan currentPlan = studyPlanMapper.selectOne(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId)
                        // 原数字状态改为枚举，code 与数据库保持一致：PLAN_STATUS_RUNNING=1
                        .eq(StudyPlan::getStatus, StudyPlanStatusEnum.RUNNING.getCode())
                        .orderByDesc(StudyPlan::getId)
                        .last("limit 1")
        );
        if (currentPlan == null) {
            return null;
        }
        return toPlanVO(currentPlan);
    }

    @Override
    public List<StudyTaskVO> listPlanTasks(Long userId, Long planId) {
        StudyPlan plan = requireOwnedPlan(userId, planId);

        List<StudyTask> taskList = studyTaskMapper.selectList(
                new LambdaQueryWrapper<StudyTask>()
                        .eq(StudyTask::getPlanId, plan.getId())
                        .orderByAsc(StudyTask::getTaskDate, StudyTask::getTaskOrder, StudyTask::getId)
        );
        if (taskList.isEmpty()) {
            return Collections.emptyList();
        }

        List<StudyTaskVO> result = new ArrayList<>();
        for (StudyTask task : taskList) {
            result.add(toTaskVO(task));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatus(Long userId, UpdateStudyTaskStatusDTO dto) {
        // 原数字状态改为枚举，code 与数据库保持一致：TASK_STATUS_TODO=0，TASK_STATUS_DONE=1
        if (dto.getStatus() == null || (!dto.getStatus().equals(StudyTaskStatusEnum.TODO.getCode())
                && !dto.getStatus().equals(StudyTaskStatusEnum.DONE.getCode()))) {
            throw new BusinessException("任务状态仅支持 0(未完成) 或 1(已完成)");
        }

        StudyTask task = studyTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        requireOwnedPlan(userId, task.getPlanId());

        studyTaskMapper.update(
                null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<StudyTask>()
                        .eq(StudyTask::getId, task.getId())
                        .set(StudyTask::getStatus, dto.getStatus())
                        .set(StudyTask::getUpdateTime, LocalDateTime.now())
        );
    }

    private void closeRunningPlans(Long userId, LocalDateTime now) {
        List<StudyPlan> runningPlans = studyPlanMapper.selectList(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId)
                        // 原数字状态改为枚举，code 与数据库保持一致：PLAN_STATUS_RUNNING=1
                        .eq(StudyPlan::getStatus, StudyPlanStatusEnum.RUNNING.getCode())
        );
        for (StudyPlan runningPlan : runningPlans) {
            studyPlanMapper.update(
                    null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<StudyPlan>()
                            .eq(StudyPlan::getId, runningPlan.getId())
                            // 原数字状态改为枚举，code 与数据库保持一致：PLAN_STATUS_FINISHED=2
                            .set(StudyPlan::getStatus, StudyPlanStatusEnum.FINISHED.getCode())
                            .set(StudyPlan::getUpdateTime, now)
            );
        }
    }

    /**
     * 任务拆分规则：
     * 1. 固定 7 天；
     * 2. 每日任务条数由时长决定；
     * 3. 每日总时长均分到各任务；
     * 4. 任务类型按目标序列循环分配。
     */
    private List<StudyTask> buildPlanTasks(StudyPlan plan, Integer level, LocalDateTime now) {
        int tasksPerDay = resolveTasksPerDay(plan.getDailyMinutes());
        int[] durationParts = splitDuration(plan.getDailyMinutes(), tasksPerDay);

        List<TaskTypeEnum> taskTypeSequence = buildTaskTypeSequence(plan.getGoalType());
        int sequenceIndex = 0;

        List<StudyTask> result = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < PLAN_DAYS; dayOffset++) {
            LocalDate taskDate = plan.getStartDate().plusDays(dayOffset);

            for (int order = 1; order <= tasksPerDay; order++) {
                TaskTypeEnum taskType = taskTypeSequence.get(sequenceIndex % taskTypeSequence.size());
                sequenceIndex++;

                StudyTask task = new StudyTask();
                task.setPlanId(plan.getId());
                task.setTaskDate(taskDate);
                task.setTaskType(taskType.getCode());
                task.setQuestionType(resolveQuestionType(taskType));
                task.setSceneType(resolveSceneType(plan.getGoalType()));
                task.setDurationMinutes(durationParts[order - 1]);
                task.setTaskOrder(order);
                // 原数字状态改为枚举，code 与数据库保持一致：TASK_STATUS_TODO=0
                task.setStatus(StudyTaskStatusEnum.TODO.getCode());
                task.setTaskTitle(buildTaskTitle(taskType, plan.getGoalType()));
                task.setTaskContent(buildTaskContent(taskType, plan.getGoalType(), level, durationParts[order - 1]));
                task.setCreateTime(now);
                task.setUpdateTime(now);
                result.add(task);
            }
        }
        return result;
    }

    private List<TaskTypeEnum> buildTaskTypeSequence(Integer goalTypeCode) {
        GoalTypeEnum goalType = GoalTypeEnum.fromCode(goalTypeCode);
        if (goalType == null) {
            throw new BusinessException("goalType only supports 1/2/3");
        }

        List<TaskTypeEnum> sequence = new ArrayList<>();
        if (goalType == GoalTypeEnum.TRAVEL) {
            Collections.addAll(sequence,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.LISTENING,
                    TaskTypeEnum.SPEAKING,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.READING,
                    TaskTypeEnum.LISTENING,
                    TaskTypeEnum.SPEAKING,
                    TaskTypeEnum.GRAMMAR);
            return sequence;
        }
        if (goalType == GoalTypeEnum.EXAM) {
            Collections.addAll(sequence,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.GRAMMAR,
                    TaskTypeEnum.READING,
                    TaskTypeEnum.LISTENING,
                    TaskTypeEnum.GRAMMAR,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.READING,
                    TaskTypeEnum.LISTENING);
            return sequence;
        }
        Collections.addAll(sequence,
                TaskTypeEnum.VOCABULARY,
                TaskTypeEnum.SPEAKING,
                TaskTypeEnum.READING,
                TaskTypeEnum.LISTENING,
                TaskTypeEnum.SPEAKING,
                TaskTypeEnum.GRAMMAR,
                TaskTypeEnum.READING,
                TaskTypeEnum.VOCABULARY);
        return sequence;
    }

    private StudyPlan requireOwnedPlan(Long userId, Long planId) {
        StudyPlan plan = studyPlanMapper.selectOne(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getId, planId)
                        .eq(StudyPlan::getUserId, userId)
                        .last("limit 1")
        );
        if (plan == null) {
            throw new BusinessException("学习计划不存在或无权限");
        }
        return plan;
    }

    private int resolveTasksPerDay(Integer dailyMinutes) {
        if (dailyMinutes == 30) {
            return 2;
        }
        if (dailyMinutes == 60) {
            return 3;
        }
        if (dailyMinutes == 90) {
            return 4;
        }
        throw new BusinessException("dailyMinutes only supports 30/60/90");
    }

    private int[] splitDuration(int totalMinutes, int partCount) {
        int[] result = new int[partCount];
        int base = totalMinutes / partCount;
        int remainder = totalMinutes % partCount;
        for (int i = 0; i < partCount; i++) {
            result[i] = base + (i < remainder ? 1 : 0);
        }
        return result;
    }

    private Integer resolveQuestionType(TaskTypeEnum taskType) {
        if (taskType == TaskTypeEnum.VOCABULARY) {
            return QuestionTypeEnum.VOCABULARY_CHOICE.getCode();
        }
        if (taskType == TaskTypeEnum.GRAMMAR) {
            return QuestionTypeEnum.GRAMMAR_CLOZE.getCode();
        }
        if (taskType == TaskTypeEnum.LISTENING) {
            return QuestionTypeEnum.LISTENING_CHOICE.getCode();
        }
        if (taskType == TaskTypeEnum.SPEAKING) {
            return QuestionTypeEnum.SPEAKING_SUBJECTIVE.getCode();
        }
        // 阅读当前无题型映射，先置空。
        return null;
    }

    private Integer resolveSceneType(Integer goalTypeCode) {
        GoalTypeEnum goalType = GoalTypeEnum.fromCode(goalTypeCode);
        if (goalType == GoalTypeEnum.TRAVEL) {
            return SceneTypeEnum.TRAVEL.getCode();
        }
        if (goalType == GoalTypeEnum.EXAM) {
            return SceneTypeEnum.EXAM.getCode();
        }
        if (goalType == GoalTypeEnum.BUSINESS) {
            return SceneTypeEnum.BUSINESS.getCode();
        }
        return SceneTypeEnum.GENERAL.getCode();
    }

    private String buildPlanName(Integer goalTypeCode, Integer levelCode, Integer dailyMinutes) {
        return GoalTypeEnum.getNameByCode(goalTypeCode) + "英语7天计划-"
                + EnglishLevelEnum.getNameByCode(levelCode) + "-" + dailyMinutes + "分钟/天";
    }

    private String buildTaskTitle(TaskTypeEnum taskType, Integer goalTypeCode) {
        return taskType.getName() + "训练（" + GoalTypeEnum.getNameByCode(goalTypeCode) + "）";
    }

    /**
     * 文案说明：
     * - 难度提示由等级枚举分流；
     * - 场景提示由目标枚举分流；
     * - 任务说明由任务类型枚举分流。
     */
    private String buildTaskContent(TaskTypeEnum taskType, Integer goalTypeCode, Integer levelCode, Integer durationMinutes) {
        String levelHint;
        if (EnglishLevelEnum.BEGINNER.getCode().equals(levelCode)) {
            levelHint = "以基础词句为主，重在建立表达信心";
        } else if (EnglishLevelEnum.INTERMEDIATE.getCode().equals(levelCode)) {
            levelHint = "以准确率与表达完整度为主";
        } else {
            levelHint = "以复杂表达与综合应用为主";
        }

        String goalHint;
        GoalTypeEnum goalType = GoalTypeEnum.fromCode(goalTypeCode);
        if (goalType == GoalTypeEnum.TRAVEL) {
            goalHint = "围绕出行、问路、点餐、酒店沟通等旅游场景";
        } else if (goalType == GoalTypeEnum.EXAM) {
            goalHint = "围绕考试高频考点与限时作答场景";
        } else {
            goalHint = "围绕会议沟通、邮件表达、商务介绍等工作场景";
        }

        String taskHint;
        if (taskType == TaskTypeEnum.VOCABULARY) {
            taskHint = "完成核心词汇记忆与短句应用";
        } else if (taskType == TaskTypeEnum.GRAMMAR) {
            taskHint = "完成重点语法规则复习与例句改写";
        } else if (taskType == TaskTypeEnum.LISTENING) {
            taskHint = "完成听力材料精听并记录关键信息";
        } else if (taskType == TaskTypeEnum.SPEAKING) {
            taskHint = "完成口语跟读与场景表达练习";
        } else {
            taskHint = "完成短文阅读并提炼主旨与细节";
        }

        return "建议学习" + durationMinutes + "分钟；" + goalHint + "；" + levelHint + "；本次任务：" + taskHint + "。";
    }

    private void validateGenerateDTO(GenerateStudyPlanDTO dto) {
        if (!GoalTypeEnum.containsCode(dto.getGoalType())) {
            throw new BusinessException("goalType only supports 1/2/3");
        }
        if (dto.getDailyMinutes() == null || (dto.getDailyMinutes() != 30
                && dto.getDailyMinutes() != 60
                && dto.getDailyMinutes() != 90)) {
            throw new BusinessException("dailyMinutes only supports 30/60/90");
        }
    }

    private Integer normalizeLevel(Integer level) {
        return EnglishLevelEnum.containsCode(level) ? level : EnglishLevelEnum.BEGINNER.getCode();
    }

    /**
     * VO 组装统一通过枚举转换名称，避免在多个地方重复 if/else。
     */
    private StudyPlanVO toPlanVO(StudyPlan plan) {
        StudyPlanVO vo = new StudyPlanVO();
        vo.setId(plan.getId());
        vo.setGoalType(plan.getGoalType());
        vo.setGoalTypeName(GoalTypeEnum.getNameByCode(plan.getGoalType()));
        vo.setCurrentLevel(plan.getCurrentLevel());
        vo.setCurrentLevelName(EnglishLevelEnum.getNameByCode(plan.getCurrentLevel()));
        vo.setDailyMinutes(plan.getDailyMinutes());
        vo.setPlanName(plan.getPlanName());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setStatus(plan.getStatus());
        return vo;
    }

    private StudyTaskVO toTaskVO(StudyTask task) {
        StudyTaskVO vo = new StudyTaskVO();
        vo.setId(task.getId());
        vo.setPlanId(task.getPlanId());
        vo.setTaskDate(task.getTaskDate());
        vo.setTaskTitle(task.getTaskTitle());
        vo.setTaskContent(task.getTaskContent());

        vo.setTaskType(task.getTaskType());
        vo.setTaskTypeName(TaskTypeEnum.getNameByCode(task.getTaskType()));

        vo.setQuestionType(task.getQuestionType());
        vo.setQuestionTypeName(QuestionTypeEnum.getNameByCode(task.getQuestionType()));

        vo.setSceneType(task.getSceneType());
        vo.setSceneTypeName(SceneTypeEnum.getNameByCode(task.getSceneType()));

        vo.setDurationMinutes(task.getDurationMinutes());
        vo.setTaskOrder(task.getTaskOrder());
        vo.setStatus(task.getStatus());
        return vo;
    }
}
