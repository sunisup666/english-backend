package com.suncan.english.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.constant.QuestionTypeConstant;
import com.suncan.english.constant.SceneTypeConstant;
import com.suncan.english.dto.plan.GenerateStudyPlanDTO;
import com.suncan.english.dto.plan.UpdateStudyTaskStatusDTO;
import com.suncan.english.entity.StudyPlan;
import com.suncan.english.entity.StudyTask;
import com.suncan.english.entity.User;
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
 * 学习计划业务实现。
 */
@Service
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final int PLAN_STATUS_RUNNING = 1;
    private static final int PLAN_STATUS_FINISHED = 2;

    private static final int TASK_STATUS_TODO = 0;
    private static final int TASK_STATUS_DONE = 1;

    private static final int GOAL_TRAVEL = 1;
    private static final int GOAL_EXAM = 2;
    private static final int GOAL_BUSINESS = 3;

    private static final int TASK_TYPE_VOCABULARY = 1;
    private static final int TASK_TYPE_GRAMMAR = 2;
    private static final int TASK_TYPE_LISTENING = 3;
    private static final int TASK_TYPE_SPEAKING = 4;
    private static final int TASK_TYPE_READING = 5;

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

        String currentLevel = normalizeLevel(user.getEnglishLevel());
        LocalDate startDate = dto.getStartDate() == null ? LocalDate.now() : dto.getStartDate();
        LocalDate endDate = startDate.plusDays(PLAN_DAYS - 1);
        LocalDateTime now = LocalDateTime.now();

        // 处理已有进行中计划：基础版按“单用户同一时间只保留一个进行中计划”处理。
        // 这样前端查询当前计划时语义明确，避免出现多个 active 计划导致展示和统计混乱。
        closeRunningPlans(userId, now);

        StudyPlan plan = new StudyPlan();
        plan.setUserId(userId);
        plan.setGoalType(dto.getGoalType());
        plan.setCurrentLevel(currentLevel);
        plan.setDailyMinutes(dto.getDailyMinutes());
        plan.setPlanName(buildPlanName(dto.getGoalType(), currentLevel, dto.getDailyMinutes()));
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setStatus(PLAN_STATUS_RUNNING);
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
                        .eq(StudyPlan::getStatus, PLAN_STATUS_RUNNING)
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
        if (dto.getStatus() == null || (dto.getStatus() != TASK_STATUS_TODO && dto.getStatus() != TASK_STATUS_DONE)) {
            throw new BusinessException("任务状态仅支持 0(未完成) 或 1(已完成)");
        }

        StudyTask task = studyTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 状态更新必须做用户归属校验，防止用户越权更新他人的任务。
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
                        .eq(StudyPlan::getStatus, PLAN_STATUS_RUNNING)
        );
        for (StudyPlan runningPlan : runningPlans) {
            studyPlanMapper.update(
                    null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<StudyPlan>()
                            .eq(StudyPlan::getId, runningPlan.getId())
                            .set(StudyPlan::getStatus, PLAN_STATUS_FINISHED)
                            .set(StudyPlan::getUpdateTime, now)
            );
        }
    }

    /**
     * 任务拆分说明：
     * 1. 当前阶段固定 7 天，保证前端展示和答辩演示稳定。
     * 2. 按每日时长决定每天任务数：30分钟=2条，60分钟=3条，90分钟=4条。
     * 3. 每条任务时长做均分，余数分配给前面的任务，保证总时长与用户输入一致。
     * 4. 任务类型顺序按学习目标做规则匹配，不做 AI 推荐，逻辑清晰可解释。
     */
    private List<StudyTask> buildPlanTasks(StudyPlan plan, String level, LocalDateTime now) {
        int tasksPerDay = resolveTasksPerDay(plan.getDailyMinutes());
        int[] durationParts = splitDuration(plan.getDailyMinutes(), tasksPerDay);

        List<Integer> taskTypeSequence = buildTaskTypeSequence(plan.getGoalType());
        int sequenceIndex = 0;

        List<StudyTask> result = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < PLAN_DAYS; dayOffset++) {
            LocalDate taskDate = plan.getStartDate().plusDays(dayOffset);

            for (int order = 1; order <= tasksPerDay; order++) {
                int taskType = taskTypeSequence.get(sequenceIndex % taskTypeSequence.size());
                sequenceIndex++;

                StudyTask task = new StudyTask();
                task.setPlanId(plan.getId());
                task.setTaskDate(taskDate);
                task.setTaskType(taskType);
                task.setQuestionType(resolveQuestionType(taskType));
                task.setSceneType(resolveSceneType(plan.getGoalType()));
                task.setDurationMinutes(durationParts[order - 1]);
                task.setTaskOrder(order);
                task.setStatus(TASK_STATUS_TODO);
                task.setTaskTitle(buildTaskTitle(taskType, plan.getGoalType()));
                task.setTaskContent(buildTaskContent(taskType, plan.getGoalType(), level, durationParts[order - 1]));
                task.setCreateTime(now);
                task.setUpdateTime(now);
                result.add(task);
            }
        }
        return result;
    }

    /**
     * 目标到任务类型的规则：
     * - 旅游：强调“听+说+词汇”，保证出行情景沟通。
     * - 考试：强调“词汇+语法+阅读”，兼顾听力。
     * - 商务：强调“口语+阅读+词汇”，补充听力与语法。
     *
     * 说明：列表是循环序列，不同天按顺序轮转，保证 7 天内任务结构有规律且易讲解。
     */
    private List<Integer> buildTaskTypeSequence(Integer goalType) {
        List<Integer> sequence = new ArrayList<>();
        if (goalType == GOAL_TRAVEL) {
            Collections.addAll(sequence,
                    TASK_TYPE_VOCABULARY,
                    TASK_TYPE_LISTENING,
                    TASK_TYPE_SPEAKING,
                    TASK_TYPE_VOCABULARY,
                    TASK_TYPE_READING,
                    TASK_TYPE_LISTENING,
                    TASK_TYPE_SPEAKING,
                    TASK_TYPE_GRAMMAR);
            return sequence;
        }
        if (goalType == GOAL_EXAM) {
            Collections.addAll(sequence,
                    TASK_TYPE_VOCABULARY,
                    TASK_TYPE_GRAMMAR,
                    TASK_TYPE_READING,
                    TASK_TYPE_LISTENING,
                    TASK_TYPE_GRAMMAR,
                    TASK_TYPE_VOCABULARY,
                    TASK_TYPE_READING,
                    TASK_TYPE_LISTENING);
            return sequence;
        }
        Collections.addAll(sequence,
                TASK_TYPE_VOCABULARY,
                TASK_TYPE_SPEAKING,
                TASK_TYPE_READING,
                TASK_TYPE_LISTENING,
                TASK_TYPE_SPEAKING,
                TASK_TYPE_GRAMMAR,
                TASK_TYPE_READING,
                TASK_TYPE_VOCABULARY);
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

    private Integer resolveQuestionType(Integer taskType) {
        if (taskType == TASK_TYPE_VOCABULARY) {
            return QuestionTypeConstant.VOCABULARY_CHOICE;
        }
        if (taskType == TASK_TYPE_GRAMMAR) {
            return QuestionTypeConstant.GRAMMAR_CLOZE;
        }
        if (taskType == TASK_TYPE_LISTENING) {
            return QuestionTypeConstant.LISTENING_CHOICE;
        }
        if (taskType == TASK_TYPE_SPEAKING) {
            return QuestionTypeConstant.SPEAKING_SUBJECTIVE;
        }
        // 阅读在当前题型常量中暂无直接映射，先置空，后续可扩展阅读题型常量。
        return null;
    }

    private int resolveSceneType(Integer goalType) {
        if (goalType == GOAL_TRAVEL) {
            return SceneTypeConstant.TRAVEL;
        }
        if (goalType == GOAL_EXAM) {
            return SceneTypeConstant.EXAM;
        }
        if (goalType == GOAL_BUSINESS) {
            return SceneTypeConstant.BUSINESS;
        }
        return SceneTypeConstant.GENERAL;
    }

    private String buildPlanName(Integer goalType, String level, Integer dailyMinutes) {
        return goalName(goalType) + "英语7天计划-" + level + "-" + dailyMinutes + "分钟/天";
    }

    private String buildTaskTitle(Integer taskType, Integer goalType) {
        return taskTypeName(taskType) + "训练（" + goalName(goalType) + "）";
    }

    /**
     * 任务内容生成说明：
     * - 内容由“目标 + 等级 + 任务类型 + 时长”拼装，确保每条任务都能解释生成依据。
     * - 等级影响难度描述：初级偏基础表达，中级强调准确率，高级强调综合输出。
     * - 目标影响场景词：旅游/考试/商务三个方向给出不同练习提示。
     */
    private String buildTaskContent(Integer taskType, Integer goalType, String level, Integer durationMinutes) {
        String levelHint;
        if ("初级".equals(level)) {
            levelHint = "以基础词句为主，重在建立表达信心";
        } else if ("中级".equals(level)) {
            levelHint = "以准确率与表达完整度为主";
        } else {
            levelHint = "以复杂表达与综合应用为主";
        }

        String goalHint;
        if (goalType == GOAL_TRAVEL) {
            goalHint = "围绕出行、问路、点餐、酒店沟通等旅游场景";
        } else if (goalType == GOAL_EXAM) {
            goalHint = "围绕考试高频考点与限时作答场景";
        } else {
            goalHint = "围绕会议沟通、邮件表达、商务介绍等工作场景";
        }

        String taskHint;
        if (taskType == TASK_TYPE_VOCABULARY) {
            taskHint = "完成核心词汇记忆与短句应用";
        } else if (taskType == TASK_TYPE_GRAMMAR) {
            taskHint = "完成重点语法规则复习与例句改写";
        } else if (taskType == TASK_TYPE_LISTENING) {
            taskHint = "完成听力材料精听并记录关键信息";
        } else if (taskType == TASK_TYPE_SPEAKING) {
            taskHint = "完成口语跟读与场景表达练习";
        } else {
            taskHint = "完成短文阅读并提炼主旨与细节";
        }

        return "建议学习" + durationMinutes + "分钟；" + goalHint + "；" + levelHint + "；本次任务：" + taskHint + "。";
    }

    private String goalName(Integer goalType) {
        if (goalType == GOAL_TRAVEL) {
            return "旅游";
        }
        if (goalType == GOAL_EXAM) {
            return "考试";
        }
        if (goalType == GOAL_BUSINESS) {
            return "商务交流";
        }
        return "通用";
    }

    private String taskTypeName(Integer taskType) {
        if (taskType == TASK_TYPE_VOCABULARY) {
            return "词汇";
        }
        if (taskType == TASK_TYPE_GRAMMAR) {
            return "语法";
        }
        if (taskType == TASK_TYPE_LISTENING) {
            return "听力";
        }
        if (taskType == TASK_TYPE_SPEAKING) {
            return "口语";
        }
        if (taskType == TASK_TYPE_READING) {
            return "阅读";
        }
        return "学习";
    }

    private void validateGenerateDTO(GenerateStudyPlanDTO dto) {
        if (dto.getGoalType() == null || (dto.getGoalType() != GOAL_TRAVEL
                && dto.getGoalType() != GOAL_EXAM
                && dto.getGoalType() != GOAL_BUSINESS)) {
            throw new BusinessException("goalType only supports 1/2/3");
        }
        if (dto.getDailyMinutes() == null || (dto.getDailyMinutes() != 30
                && dto.getDailyMinutes() != 60
                && dto.getDailyMinutes() != 90)) {
            throw new BusinessException("dailyMinutes only supports 30/60/90");
        }
    }

    private String normalizeLevel(String level) {
        String trimLevel = trimToNull(level);
        return trimLevel == null ? "初级" : trimLevel;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private StudyPlanVO toPlanVO(StudyPlan plan) {
        StudyPlanVO vo = new StudyPlanVO();
        vo.setId(plan.getId());
        vo.setGoalType(plan.getGoalType());
        vo.setCurrentLevel(plan.getCurrentLevel());
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
        vo.setQuestionType(task.getQuestionType());
        vo.setSceneType(task.getSceneType());
        vo.setDurationMinutes(task.getDurationMinutes());
        vo.setTaskOrder(task.getTaskOrder());
        vo.setStatus(task.getStatus());
        return vo;
    }
}
