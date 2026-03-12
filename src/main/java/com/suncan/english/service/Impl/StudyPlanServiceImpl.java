package com.suncan.english.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.suncan.english.dto.plan.GenerateStudyPlanDTO;
import com.suncan.english.dto.plan.StudyTaskQueryDTO;
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
import com.suncan.english.vo.plan.StudyTaskPageVO;
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
 * 1. 计划和任务仍按 Integer 编码落库，保证 SQL 查询和统计稳定。
 * 2. 业务层通过枚举表达语义，避免魔法值散落。
 * 3. 本次整理后，任务类型仅保留与题库一致的 4 类：词汇/语法/听力/口语。
 */
@Service
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final int PLAN_DAYS = 7;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 10L;
    private static final long MAX_SIZE = 50L;

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

    /**
     * 生成学习计划与初始任务。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanVO generateStudyPlan(Long userId, GenerateStudyPlanDTO dto) {
        validateGenerateDTO(dto);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 计划生成时记录“当前等级快照”，保证后续回溯计划依据时有据可查。
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
        // 原数字状态改为枚举，code 与数据库保持一致：1=进行中。
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

    /**
     * 查询当前进行中的学习计划。
     */
    @Override
    public StudyPlanVO getCurrentPlan(Long userId) {
        StudyPlan currentPlan = studyPlanMapper.selectOne(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId)
                        // 原数字状态改为枚举，code 与数据库保持一致：1=进行中。
                        .eq(StudyPlan::getStatus, StudyPlanStatusEnum.RUNNING.getCode())
                        .orderByDesc(StudyPlan::getId)
                        .last("limit 1")
        );
        if (currentPlan == null) {
            return null;
        }
        return toPlanVO(currentPlan);
    }

    /**
     * 分页查询学习任务列表。
     *
     * 说明：
     * 1. 保持原接口路径不变，只升级查询能力，避免新增 today/all/finished 等重复接口；
     * 2. 通过可选条件实现“同一接口多视图”：传今天日期可看当天任务，不传日期即看全部任务；
     * 3. 返回分页结构更适合历史任务场景，数据量增长后也更稳定。
     */
    @Override
    public StudyTaskPageVO listPlanTasks(Long userId, StudyTaskQueryDTO queryDTO) {
        // 统一在 service 层做参数校验与兜底，便于 controller、其他调用方复用同一套规则。
        validateTaskListQuery(queryDTO);

        // 先做计划归属校验，避免越权读取他人任务。
        StudyPlan plan = requireOwnedPlan(userId, queryDTO.getPlanId());

        // current/size 在 service 层统一归一化，防止前端传入异常分页值导致查询行为不可控。
        long current = normalizeCurrent(queryDTO.getCurrent());
        long size = normalizeSize(queryDTO.getSize());
        long offset = (current - 1L) * size;

        // 构建通用查询条件：计划ID必传，日期/状态/类型按可选参数动态拼接。
        LambdaQueryWrapper<StudyTask> queryWrapper = buildTaskListWrapper(plan.getId(), queryDTO, false);

        // 先查总数，再查分页数据，便于前端分页控件展示总页数和总条数。
        Long totalValue = studyTaskMapper.selectCount(queryWrapper);
        long total = totalValue == null ? 0L : totalValue;

        List<StudyTaskVO> records = new ArrayList<>();
        if (total > 0) {
            // 查询当前页数据时保持稳定排序，避免前端翻页或刷新后顺序跳动。
            LambdaQueryWrapper<StudyTask> pageWrapper = buildTaskListWrapper(plan.getId(), queryDTO, true)
                    .last("limit " + offset + "," + size);
            List<StudyTask> taskList = studyTaskMapper.selectList(pageWrapper);

            // records 仍沿用原有 StudyTaskVO，并补齐任务类型/题型/场景中文名称。
            for (StudyTask task : taskList) {
                records.add(toTaskVO(task));
            }
        }

        StudyTaskPageVO pageVO = new StudyTaskPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    /**
     * 查询单个学习任务详情（仅任务元数据）。
     *
     * 说明：
     * 1. 这里必须做任务归属校验，避免用户越权查看他人的任务信息；
     * 2. 该接口只负责返回任务元数据，不加载练习题目；
     * 3. 练习题目由 Practice 模块按规则动态生成，职责分离后更利于维护与前端联调。
     */
    @Override
    public StudyTaskVO taskDetail(Long userId, Long taskId) {
        StudyTask task = requireOwnedTask(userId, taskId);
        return toTaskVO(task);
    }

    /**
     * 更新任务完成状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatus(Long userId, UpdateStudyTaskStatusDTO dto) {
        // 原数字状态改为枚举，code 与数据库保持一致：0=待完成，1=已完成。
        if (dto.getStatus() == null
                || (!dto.getStatus().equals(StudyTaskStatusEnum.TODO.getCode())
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
                new LambdaUpdateWrapper<StudyTask>()
                        .eq(StudyTask::getId, task.getId())
                        .set(StudyTask::getStatus, dto.getStatus())
                        .set(StudyTask::getUpdateTime, LocalDateTime.now())
        );
    }

    private void closeRunningPlans(Long userId, LocalDateTime now) {
        List<StudyPlan> runningPlans = studyPlanMapper.selectList(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId)
                        // 原数字状态改为枚举，code 与数据库保持一致：1=进行中。
                        .eq(StudyPlan::getStatus, StudyPlanStatusEnum.RUNNING.getCode())
        );
        for (StudyPlan runningPlan : runningPlans) {
            studyPlanMapper.update(
                    null,
                    new LambdaUpdateWrapper<StudyPlan>()
                            .eq(StudyPlan::getId, runningPlan.getId())
                            // 原数字状态改为枚举，code 与数据库保持一致：2=已完成。
                            .set(StudyPlan::getStatus, StudyPlanStatusEnum.FINISHED.getCode())
                            .set(StudyPlan::getUpdateTime, now)
            );
        }
    }

    /**
     * 任务拆分规则：
     * 1. 固定 7 天；
     * 2. 每日任务条数由学习时长决定；
     * 3. 每日总时长按任务数均分；
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
                // 原数字状态改为枚举，code 与数据库保持一致：0=待完成。
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

        // 为什么删除阅读任务分支：
        // 1. 当前题库 question_type 只有 1~4（词汇/语法/听力/口语），没有阅读题型。
        // 2. 任务类型必须与题库题型一致，否则会出现“任务能生成但无法正常出题/提交”的断链。
        // 3. 若后续新增阅读题型，再恢复阅读任务分支更合理，能保证上线即闭环。
        List<TaskTypeEnum> sequence = new ArrayList<>();
        if (goalType == GoalTypeEnum.TRAVEL) {
            Collections.addAll(sequence,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.LISTENING,
                    TaskTypeEnum.SPEAKING,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.LISTENING,
                    TaskTypeEnum.SPEAKING,
                    TaskTypeEnum.GRAMMAR,
                    TaskTypeEnum.VOCABULARY);
            return sequence;
        }
        if (goalType == GoalTypeEnum.EXAM) {
            Collections.addAll(sequence,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.GRAMMAR,
                    TaskTypeEnum.LISTENING,
                    TaskTypeEnum.GRAMMAR,
                    TaskTypeEnum.VOCABULARY,
                    TaskTypeEnum.LISTENING,
                    TaskTypeEnum.GRAMMAR,
                    TaskTypeEnum.VOCABULARY);
            return sequence;
        }
        Collections.addAll(sequence,
                TaskTypeEnum.VOCABULARY,
                TaskTypeEnum.SPEAKING,
                TaskTypeEnum.LISTENING,
                TaskTypeEnum.SPEAKING,
                TaskTypeEnum.GRAMMAR,
                TaskTypeEnum.VOCABULARY,
                TaskTypeEnum.LISTENING,
                TaskTypeEnum.SPEAKING);
        return sequence;
    }

    /**
     * 校验任务列表查询参数。
     *
     * 说明：
     * 1. 将基础校验放在 service 层，能保证 controller、定时任务或其他调用入口都复用同一规则；
     * 2. planId 是归属校验与查询主条件，必须有值；
     * 3. status/taskType 属于可选筛选，传了就校验合法值，避免无效参数造成脏查询。
     */
    private void validateTaskListQuery(StudyTaskQueryDTO queryDTO) {
        if (queryDTO == null || queryDTO.getPlanId() == null) {
            throw new BusinessException("planId cannot be null");
        }

        if (queryDTO.getStatus() != null && !isValidTaskStatus(queryDTO.getStatus())) {
            throw new BusinessException("status only supports 0/1");
        }

        if (queryDTO.getTaskType() != null && TaskTypeEnum.fromCode(queryDTO.getTaskType()) == null) {
            throw new BusinessException("taskType only supports 1/2/3/4");
        }
    }

    /**
     * 归一化当前页参数：小于 1 时按 1 处理。
     */
    private long normalizeCurrent(Long current) {
        if (current == null || current < 1L) {
            return DEFAULT_CURRENT;
        }
        return current;
    }

    /**
     * 归一化每页条数参数：小于 1 按 10，超过 50 按 50。
     */
    private long normalizeSize(Long size) {
        if (size == null || size < 1L) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    /**
     * 构建任务列表查询条件。
     *
     * 说明：
     * 1. taskDate/status/taskType 都是可选条件，按“有值才拼接”实现灵活筛选；
     * 2. 排序统一使用 task_date asc、task_order asc、id asc，保证分页前后顺序稳定；
     * 3. 稳定排序可以避免前端刷新、翻页时同一批数据顺序跳动。
     */
    private LambdaQueryWrapper<StudyTask> buildTaskListWrapper(Long planId, StudyTaskQueryDTO queryDTO, boolean withOrder) {
        LambdaQueryWrapper<StudyTask> wrapper = new LambdaQueryWrapper<StudyTask>()
                .eq(StudyTask::getPlanId, planId);

        if (queryDTO.getTaskDate() != null) {
            wrapper.eq(StudyTask::getTaskDate, queryDTO.getTaskDate());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(StudyTask::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getTaskType() != null) {
            wrapper.eq(StudyTask::getTaskType, queryDTO.getTaskType());
        }
        if (withOrder) {
            wrapper.orderByAsc(StudyTask::getTaskDate, StudyTask::getTaskOrder, StudyTask::getId);
        }
        return wrapper;
    }

    /**
     * 校验任务状态是否合法。
     */
    private boolean isValidTaskStatus(Integer status) {
        return StudyTaskStatusEnum.TODO.getCode().equals(status)
                || StudyTaskStatusEnum.DONE.getCode().equals(status);
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

    /**
     * 校验任务归属并返回任务实体。
     *
     * 说明：
     * 任务详情是用户私有学习数据，必须先校验 taskId 对应计划是否属于当前用户，
     * 这样可以从后端根源上防止越权访问。
     */
    private StudyTask requireOwnedTask(Long userId, Long taskId) {
        StudyTask task = studyTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("学习任务不存在");
        }
        requireOwnedPlan(userId, task.getPlanId());
        return task;
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
        throw new BusinessException("unsupported taskType: " + taskType);
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
     * 任务文案规则：
     * 1. 难度提示由等级枚举分流；
     * 2. 场景提示由目标枚举分流；
     * 3. 任务说明由任务类型枚举分流（仅 4 类）。
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
            throw new BusinessException("unsupported taskType: " + taskType);
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
