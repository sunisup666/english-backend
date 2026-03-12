package com.suncan.english.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.suncan.english.dto.practice.PracticeAnswerItemDTO;
import com.suncan.english.dto.practice.PracticeRecordQueryDTO;
import com.suncan.english.dto.practice.SubmitPracticeDTO;
import com.suncan.english.entity.Question;
import com.suncan.english.entity.QuestionOption;
import com.suncan.english.entity.StudyPlan;
import com.suncan.english.entity.StudyTask;
import com.suncan.english.entity.User;
import com.suncan.english.entity.UserPracticeAnswer;
import com.suncan.english.entity.UserPracticeRecord;
import com.suncan.english.enums.EnglishLevelEnum;
import com.suncan.english.enums.QuestionDifficultyEnum;
import com.suncan.english.enums.QuestionTypeEnum;
import com.suncan.english.enums.SceneTypeEnum;
import com.suncan.english.enums.StudyTaskStatusEnum;
import com.suncan.english.enums.TaskTypeEnum;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.mapper.QuestionMapper;
import com.suncan.english.mapper.QuestionOptionMapper;
import com.suncan.english.mapper.StudyPlanMapper;
import com.suncan.english.mapper.StudyTaskMapper;
import com.suncan.english.mapper.UserMapper;
import com.suncan.english.mapper.UserPracticeAnswerMapper;
import com.suncan.english.mapper.UserPracticeRecordMapper;
import com.suncan.english.service.PracticeService;
import com.suncan.english.vo.practice.PracticeQuestionAnswerDetailVO;
import com.suncan.english.vo.practice.PracticeRecordDetailVO;
import com.suncan.english.vo.practice.PracticeRecordItemVO;
import com.suncan.english.vo.practice.PracticeRecordPageVO;
import com.suncan.english.vo.practice.PracticeSubmitResultVO;
import com.suncan.english.vo.practice.PracticeTaskVO;
import com.suncan.english.vo.test.QuestionOptionVO;
import com.suncan.english.vo.test.QuestionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学习任务执行与训练记录实现。
 *
 * 关键设计说明（答辩可直接使用）：
 * 1. 训练记录与测试记录分开存储：避免“能力评估数据”和“日常训练数据”混用。
 * 2. 任务详情与练习内容分离：任务接口管元数据，练习接口管动态取题和作答结果。
 * 3. 训练记录详情支持题目+答案回显：便于用户复盘，也方便教师/答辩展示闭环。
 */
@Service
public class PracticeServiceImpl implements PracticeService {

    /** 训练记录状态：已提交。 */
    private static final int PRACTICE_RECORD_STATUS_SUBMITTED = 1;

    private final StudyTaskMapper studyTaskMapper;
    private final StudyPlanMapper studyPlanMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final UserPracticeRecordMapper userPracticeRecordMapper;
    private final UserPracticeAnswerMapper userPracticeAnswerMapper;

    public PracticeServiceImpl(StudyTaskMapper studyTaskMapper,
                               StudyPlanMapper studyPlanMapper,
                               UserMapper userMapper,
                               QuestionMapper questionMapper,
                               QuestionOptionMapper questionOptionMapper,
                               UserPracticeRecordMapper userPracticeRecordMapper,
                               UserPracticeAnswerMapper userPracticeAnswerMapper) {
        this.studyTaskMapper = studyTaskMapper;
        this.studyPlanMapper = studyPlanMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.userPracticeRecordMapper = userPracticeRecordMapper;
        this.userPracticeAnswerMapper = userPracticeAnswerMapper;
    }

    /**
     * 获取任务练习内容。
     *
     * 说明：
     * 任务页面只在这里动态取题，不在学习计划任务详情接口中重复加载题目。
     * 这样可以保证“任务元数据展示”和“实际做题入口”职责清晰。
     */
    @Override
    public PracticeTaskVO getTaskPractice(Long userId, Long taskId) {
        StudyTask task = requireOwnedTask(userId, taskId);
        User user = requireUser(userId);

        int expectCount = resolveTargetCountByTaskType(task.getTaskType());
        if (expectCount <= 0) {
            throw new BusinessException("当前任务类型暂不支持练习");
        }

        List<Question> candidateList = loadCandidateQuestionsByRule(task, user.getEnglishLevel());
        List<Question> pickedQuestions = pickQuestionsForDisplay(candidateList, expectCount);
        Map<Long, List<QuestionOption>> optionMap = loadOptionMap(pickedQuestions);

        PracticeTaskVO vo = new PracticeTaskVO();
        vo.setTaskId(task.getId());
        vo.setPlanId(task.getPlanId());
        vo.setTaskType(task.getTaskType());
        vo.setTaskTypeName(TaskTypeEnum.getNameByCode(task.getTaskType()));
        vo.setQuestionType(task.getQuestionType());
        vo.setQuestionTypeName(QuestionTypeEnum.getNameByCode(task.getQuestionType()));
        vo.setSceneType(task.getSceneType());
        vo.setSceneTypeName(SceneTypeEnum.getNameByCode(task.getSceneType()));
        vo.setTaskTitle(task.getTaskTitle());
        vo.setTaskContent(task.getTaskContent());
        vo.setDurationMinutes(task.getDurationMinutes());
        vo.setStatus(task.getStatus());
        vo.setTotalCount(pickedQuestions.size());
        vo.setQuestionList(toQuestionVOList(pickedQuestions, optionMap));
        return vo;
    }

    /**
     * 提交任务练习结果。
     *
     * 说明：
     * 先保存训练记录与作答明细，再更新任务状态，保证“已完成任务”一定可追溯到训练记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeSubmitResultVO submitPractice(Long userId, SubmitPracticeDTO dto) {
        StudyTask task = requireOwnedTask(userId, dto.getTaskId());
        User user = requireUser(userId);
        int expectCount = resolveTargetCountByTaskType(task.getTaskType());

        List<Question> candidateList = loadCandidateQuestionsByRule(task, user.getEnglishLevel());
        if (expectCount <= 0 || candidateList.isEmpty()) {
            throw new BusinessException("当前任务暂无可提交题目");
        }

        Map<Long, Question> candidateMap = candidateList.stream()
                .collect(Collectors.toMap(Question::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        Map<Long, PracticeAnswerItemDTO> answerMap = normalizeAnswerMap(dto.getAnswers());
        if (answerMap.isEmpty()) {
            throw new BusinessException("answers cannot be empty");
        }
        if (answerMap.size() > expectCount) {
            throw new BusinessException("提交题量超过当前任务上限");
        }

        int totalCount = 0;
        int correctCount = 0;
        int totalScore = 0;
        LocalDateTime submitTime = LocalDateTime.now();
        int durationSeconds = resolveDurationSeconds(dto.getStartTime(), submitTime);

        List<UserPracticeAnswer> answerEntities = new ArrayList<>();
        for (Map.Entry<Long, PracticeAnswerItemDTO> entry : answerMap.entrySet()) {
            Long questionId = entry.getKey();
            PracticeAnswerItemDTO answerItem = entry.getValue();
            Question question = candidateMap.get(questionId);
            if (question == null) {
                throw new BusinessException("存在不属于当前任务的题目: " + questionId);
            }

            totalCount++;
            Integer questionType = question.getQuestionType();
            int questionScore = question.getScore() == null ? 0 : question.getScore();

            String userAnswer = resolveUserAnswer(questionType, answerItem);
            String answerText = trimToNull(answerItem.getAnswerText());
            String audioAnswerUrl = trimToNull(answerItem.getAudioAnswerUrl());

            boolean correct;
            int earnedScore;
            // 口语题当前阶段只记录，不做复杂自动评分；其余题型按文本规范化后对比。
            if (QuestionTypeEnum.isSpeaking(questionType)) {
                correct = false;
                earnedScore = 0;
            } else {
                correct = isTextEqualIgnoreCaseAndSpace(userAnswer, question.getStandardAnswer());
                earnedScore = correct ? questionScore : 0;
            }

            if (correct) {
                correctCount++;
            }
            totalScore += earnedScore;

            UserPracticeAnswer answerEntity = new UserPracticeAnswer();
            answerEntity.setQuestionId(questionId);
            answerEntity.setUserAnswer(userAnswer);
            answerEntity.setAnswerText(answerText);
            answerEntity.setAudioAnswerUrl(audioAnswerUrl);
            answerEntity.setIsCorrect(correct ? 1 : 0);
            answerEntity.setScore(earnedScore);
            answerEntity.setCreateTime(submitTime);
            answerEntities.add(answerEntity);
        }

        UserPracticeRecord record = new UserPracticeRecord();
        record.setUserId(userId);
        record.setPlanId(task.getPlanId());
        record.setTaskId(task.getId());
        record.setTaskType(task.getTaskType());
        record.setQuestionType(task.getQuestionType());
        record.setSceneType(task.getSceneType());
        record.setTotalCount(totalCount);
        record.setCorrectCount(correctCount);
        record.setTotalScore(totalScore);
        record.setStatus(PRACTICE_RECORD_STATUS_SUBMITTED);
        record.setStartTime(dto.getStartTime());
        record.setSubmitTime(submitTime);
        record.setDurationSeconds(durationSeconds);
        record.setCreateTime(submitTime);
        record.setUpdateTime(submitTime);
        userPracticeRecordMapper.insert(record);

        for (UserPracticeAnswer answerEntity : answerEntities) {
            answerEntity.setPracticeRecordId(record.getId());
            userPracticeAnswerMapper.insert(answerEntity);
        }

        int updateRows = studyTaskMapper.update(
                null,
                new LambdaUpdateWrapper<StudyTask>()
                        .eq(StudyTask::getId, task.getId())
                        .set(StudyTask::getStatus, StudyTaskStatusEnum.DONE.getCode())
                        .set(StudyTask::getUpdateTime, submitTime)
        );
        if (updateRows < 1) {
            throw new BusinessException("更新任务状态失败");
        }

        PracticeSubmitResultVO vo = new PracticeSubmitResultVO();
        vo.setRecordId(record.getId());
        vo.setTaskId(task.getId());
        vo.setTotalCount(totalCount);
        vo.setCorrectCount(correctCount);
        vo.setTotalScore(totalScore);
        vo.setDurationSeconds(durationSeconds);
        return vo;
    }

    /**
     * 查询训练记录详情。
     *
     * 说明：
     * 详情页需要回显题目与用户答案，便于复盘和结果解释，因此会联查题目表与作答明细表。
     */
    @Override
    public PracticeRecordDetailVO recordDetail(Long userId, Long recordId) {
        UserPracticeRecord record = requireOwnedRecord(userId, recordId);

        List<UserPracticeAnswer> answerList = userPracticeAnswerMapper.selectList(
                new LambdaQueryWrapper<UserPracticeAnswer>()
                        .eq(UserPracticeAnswer::getPracticeRecordId, recordId)
                        .orderByAsc(UserPracticeAnswer::getId)
        );
        if (answerList.isEmpty()) {
            return toRecordDetailVO(record, Collections.emptyList());
        }

        List<Long> questionIds = answerList.stream().map(UserPracticeAnswer::getQuestionId).distinct().collect(Collectors.toList());
        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getId, questionIds)
        );
        Map<Long, Question> questionMap = questionList.stream()
                .collect(Collectors.toMap(Question::getId, item -> item, (a, b) -> a, LinkedHashMap::new));

        // 选择题需要展示 options，非选择题固定返回空数组，避免前端额外做 null 判断。
        Set<Long> choiceQuestionIds = questionList.stream()
                .filter(item -> QuestionTypeEnum.isChoice(item.getQuestionType()))
                .map(Question::getId)
                .collect(Collectors.toSet());
        Map<Long, List<QuestionOption>> optionMap = loadOptionMapByQuestionIds(choiceQuestionIds);

        List<PracticeQuestionAnswerDetailVO> detailList = new ArrayList<>();
        for (UserPracticeAnswer answer : answerList) {
            Question question = questionMap.get(answer.getQuestionId());
            PracticeQuestionAnswerDetailVO detailVO = new PracticeQuestionAnswerDetailVO();
            detailVO.setQuestionId(answer.getQuestionId());
            detailVO.setUserAnswer(answer.getUserAnswer());
            detailVO.setAnswerText(answer.getAnswerText());
            detailVO.setAudioAnswerUrl(answer.getAudioAnswerUrl());
            detailVO.setIsCorrect(answer.getIsCorrect());
            detailVO.setScore(answer.getScore());

            if (question == null) {
                // 题目被下架/删除时仍保留答题痕迹，保障历史记录完整性。
                detailVO.setOptionList(Collections.emptyList());
                detailList.add(detailVO);
                continue;
            }

            detailVO.setQuestionType(question.getQuestionType());
            detailVO.setQuestionTypeName(QuestionTypeEnum.getNameByCode(question.getQuestionType()));
            detailVO.setSceneType(question.getSceneType());
            detailVO.setSceneTypeName(SceneTypeEnum.getNameByCode(question.getSceneType()));
            detailVO.setTitle(question.getTitle());
            detailVO.setContent(question.getContent());
            detailVO.setAudioUrl(question.getAudioUrl());
            detailVO.setStandardAnswer(question.getStandardAnswer());
            detailVO.setAnalysis(question.getAnalysis());
            if (QuestionTypeEnum.isChoice(question.getQuestionType())) {
                detailVO.setOptionList(toOptionVOList(optionMap.getOrDefault(question.getId(), Collections.emptyList())));
            } else {
                // 口语题/填空题当前阶段没有选项，返回空数组而非 null。
                detailVO.setOptionList(Collections.emptyList());
            }
            detailList.add(detailVO);
        }

        return toRecordDetailVO(record, detailList);
    }

    /**
     * 查询训练记录分页列表。
     *
     * 说明：
     * 1. 支持按计划/任务筛选，便于前端在不同入口复用同一查询接口；
     * 2. 返回分页结构，适合历史记录页面滚动加载和后续扩展。
     */
    @Override
    public PracticeRecordPageVO recordList(Long userId, PracticeRecordQueryDTO queryDTO) {
        PracticeRecordQueryDTO query = queryDTO == null ? new PracticeRecordQueryDTO() : queryDTO;
        long current = query.getCurrent() == null || query.getCurrent() < 1 ? 1 : query.getCurrent();
        long size = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 50);

        LambdaQueryWrapper<UserPracticeRecord> countWrapper = buildRecordListWrapper(userId, query);
        long total = userPracticeRecordMapper.selectCount(countWrapper);
        if (total == 0) {
            PracticeRecordPageVO empty = new PracticeRecordPageVO();
            empty.setCurrent(current);
            empty.setSize(size);
            empty.setTotal(0L);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        long offset = (current - 1) * size;
        List<UserPracticeRecord> recordList = userPracticeRecordMapper.selectList(
                buildRecordListWrapper(userId, query)
                        .orderByDesc(UserPracticeRecord::getSubmitTime, UserPracticeRecord::getId)
                        .last("limit " + offset + "," + size)
        );

        List<PracticeRecordItemVO> records = new ArrayList<>();
        for (UserPracticeRecord record : recordList) {
            PracticeRecordItemVO itemVO = new PracticeRecordItemVO();
            itemVO.setRecordId(record.getId());
            itemVO.setTaskId(record.getTaskId());
            itemVO.setPlanId(record.getPlanId());
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
     * 构建训练记录列表查询条件。
     */
    private LambdaQueryWrapper<UserPracticeRecord> buildRecordListWrapper(Long userId, PracticeRecordQueryDTO queryDTO) {
        return new LambdaQueryWrapper<UserPracticeRecord>()
                .eq(UserPracticeRecord::getUserId, userId)
                .eq(queryDTO.getPlanId() != null, UserPracticeRecord::getPlanId, queryDTO.getPlanId())
                .eq(queryDTO.getTaskId() != null, UserPracticeRecord::getTaskId, queryDTO.getTaskId());
    }

    /**
     * 校验任务归属并返回任务。
     */
    private StudyTask requireOwnedTask(Long userId, Long taskId) {
        StudyTask task = studyTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("学习任务不存在");
        }
        StudyPlan plan = studyPlanMapper.selectOne(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getId, task.getPlanId())
                        .eq(StudyPlan::getUserId, userId)
                        .last("limit 1")
        );
        if (plan == null) {
            throw new BusinessException("任务不属于当前用户");
        }
        return task;
    }

    /**
     * 校验训练记录归属并返回记录。
     */
    private UserPracticeRecord requireOwnedRecord(Long userId, Long recordId) {
        UserPracticeRecord record = userPracticeRecordMapper.selectOne(
                new LambdaQueryWrapper<UserPracticeRecord>()
                        .eq(UserPracticeRecord::getId, recordId)
                        .eq(UserPracticeRecord::getUserId, userId)
                        .last("limit 1")
        );
        if (record == null) {
            throw new BusinessException("训练记录不存在或无权限");
        }
        return record;
    }

    /**
     * 查询并校验用户。
     */
    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 按任务规则加载候选题。
     */
    private List<Question> loadCandidateQuestionsByRule(StudyTask task, Integer englishLevel) {
        if (task.getQuestionType() == null) {
            return Collections.emptyList();
        }
        QuestionDifficultyEnum difficultyEnum = resolveDifficultyByUserLevel(englishLevel);
        if (difficultyEnum == null) {
            return Collections.emptyList();
        }
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getStatus, 1)
                        .eq(Question::getQuestionType, task.getQuestionType())
                        .eq(task.getSceneType() != null, Question::getSceneType, task.getSceneType())
                        .eq(Question::getDifficulty, difficultyEnum.getCode())
        );
    }

    /**
     * 候选题随机截取前 N 条，形成实际练习题。
     */
    private List<Question> pickQuestionsForDisplay(List<Question> candidateList, int expectCount) {
        if (candidateList == null || candidateList.isEmpty() || expectCount <= 0) {
            return Collections.emptyList();
        }
        List<Question> shuffled = new ArrayList<>(candidateList);
        Collections.shuffle(shuffled);
        return shuffled.size() <= expectCount ? shuffled : new ArrayList<>(shuffled.subList(0, expectCount));
    }

    /**
     * 按任务类型决定题量。
     */
    private int resolveTargetCountByTaskType(Integer taskType) {
        if (TaskTypeEnum.VOCABULARY.getCode().equals(taskType)) {
            return 5;
        }
        if (TaskTypeEnum.GRAMMAR.getCode().equals(taskType)) {
            return 5;
        }
        if (TaskTypeEnum.LISTENING.getCode().equals(taskType)) {
            return 3;
        }
        if (TaskTypeEnum.SPEAKING.getCode().equals(taskType)) {
            return 1;
        }
        throw new BusinessException("unsupported taskType: " + taskType);
    }

    /**
     * 用户等级映射到题目难度。
     */
    private QuestionDifficultyEnum resolveDifficultyByUserLevel(Integer englishLevel) {
        if (EnglishLevelEnum.INTERMEDIATE.getCode().equals(englishLevel)) {
            return QuestionDifficultyEnum.MEDIUM;
        }
        if (EnglishLevelEnum.ADVANCED.getCode().equals(englishLevel)) {
            return QuestionDifficultyEnum.HARD;
        }
        return QuestionDifficultyEnum.EASY;
    }

    /**
     * 按题目列表加载选择题选项。
     */
    private Map<Long, List<QuestionOption>> loadOptionMap(List<Question> questionList) {
        Set<Long> questionIds = questionList.stream()
                .filter(item -> QuestionTypeEnum.isChoice(item.getQuestionType()))
                .map(Question::getId)
                .collect(Collectors.toSet());
        return loadOptionMapByQuestionIds(questionIds);
    }

    /**
     * 按题目ID集合加载选项并按 questionId 分组。
     */
    private Map<Long, List<QuestionOption>> loadOptionMapByQuestionIds(Set<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QuestionOption> optionList = questionOptionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>()
                        .in(QuestionOption::getQuestionId, questionIds)
                        .orderByAsc(QuestionOption::getSortOrder, QuestionOption::getId)
        );
        return optionList.stream().collect(Collectors.groupingBy(QuestionOption::getQuestionId));
    }

    /**
     * 题目实体转换为练习题目 VO。
     */
    private List<QuestionVO> toQuestionVOList(List<Question> questionList, Map<Long, List<QuestionOption>> optionMap) {
        List<QuestionVO> result = new ArrayList<>();
        for (Question question : questionList) {
            QuestionVO vo = new QuestionVO();
            vo.setQuestionId(question.getId());
            vo.setQuestionType(question.getQuestionType());
            vo.setQuestionTypeName(QuestionTypeEnum.getNameByCode(question.getQuestionType()));
            vo.setSceneType(question.getSceneType());
            vo.setSceneTypeName(SceneTypeEnum.getNameByCode(question.getSceneType()));
            vo.setTitle(question.getTitle());
            vo.setContent(question.getContent());
            vo.setAudioUrl(question.getAudioUrl());
            vo.setScore(question.getScore());
            vo.setDifficulty(question.getDifficulty());
            vo.setDifficultyCode(question.getDifficulty());
            vo.setDifficultyName(QuestionDifficultyEnum.getNameByCode(question.getDifficulty()));
            vo.setSortOrder(question.getSortOrder());
            if (QuestionTypeEnum.isChoice(question.getQuestionType())) {
                vo.setOptions(toOptionVOList(optionMap.getOrDefault(question.getId(), Collections.emptyList())));
            } else {
                vo.setOptions(Collections.emptyList());
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 组装训练记录详情 VO。
     */
    private PracticeRecordDetailVO toRecordDetailVO(UserPracticeRecord record, List<PracticeQuestionAnswerDetailVO> questionAnswerList) {
        PracticeRecordDetailVO detailVO = new PracticeRecordDetailVO();
        detailVO.setRecordId(record.getId());
        detailVO.setTaskId(record.getTaskId());
        detailVO.setPlanId(record.getPlanId());
        detailVO.setTaskType(record.getTaskType());
        detailVO.setTaskTypeName(TaskTypeEnum.getNameByCode(record.getTaskType()));
        detailVO.setQuestionType(record.getQuestionType());
        detailVO.setQuestionTypeName(QuestionTypeEnum.getNameByCode(record.getQuestionType()));
        detailVO.setSceneType(record.getSceneType());
        detailVO.setSceneTypeName(SceneTypeEnum.getNameByCode(record.getSceneType()));
        detailVO.setTotalCount(record.getTotalCount());
        detailVO.setCorrectCount(record.getCorrectCount());
        detailVO.setTotalScore(record.getTotalScore());
        detailVO.setDurationSeconds(record.getDurationSeconds());
        detailVO.setStartTime(record.getStartTime());
        detailVO.setSubmitTime(record.getSubmitTime());
        detailVO.setQuestionAnswerList(questionAnswerList);
        return detailVO;
    }

    /**
     * 选项实体转 VO。
     */
    private List<QuestionOptionVO> toOptionVOList(List<QuestionOption> optionList) {
        List<QuestionOptionVO> result = new ArrayList<>();
        for (QuestionOption option : optionList) {
            QuestionOptionVO optionVO = new QuestionOptionVO();
            optionVO.setOptionLabel(option.getOptionLabel());
            optionVO.setOptionContent(option.getOptionContent());
            optionVO.setSortOrder(option.getSortOrder());
            result.add(optionVO);
        }
        return result;
    }

    /**
     * 提交答案列表转 Map，自动去掉空项，后出现的同题答案会覆盖前值。
     */
    private Map<Long, PracticeAnswerItemDTO> normalizeAnswerMap(List<PracticeAnswerItemDTO> answers) {
        Map<Long, PracticeAnswerItemDTO> answerMap = new LinkedHashMap<>();
        if (answers == null) {
            return answerMap;
        }
        for (PracticeAnswerItemDTO item : answers) {
            if (item == null || item.getQuestionId() == null) {
                continue;
            }
            answerMap.put(item.getQuestionId(), item);
        }
        return answerMap;
    }

    /**
     * 按题型提取用户答案主值。
     */
    private String resolveUserAnswer(Integer questionType, PracticeAnswerItemDTO answerItem) {
        if (answerItem == null) {
            return null;
        }
        if (QuestionTypeEnum.isSpeaking(questionType)) {
            String answerText = trimToNull(answerItem.getAnswerText());
            return answerText != null ? answerText : trimToNull(answerItem.getAnswer());
        }
        return trimToNull(answerItem.getAnswer());
    }

    /**
     * 文本比对：忽略大小写与空格。
     */
    private boolean isTextEqualIgnoreCaseAndSpace(String left, String right) {
        String leftNormalized = normalizeForCompare(left);
        String rightNormalized = normalizeForCompare(right);
        if (leftNormalized == null || rightNormalized == null) {
            return false;
        }
        return leftNormalized.equals(rightNormalized);
    }

    /**
     * 文本标准化。
     */
    private String normalizeForCompare(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        return text.replace(" ", "").toUpperCase();
    }

    /**
     * 计算提交时长（秒）。
     */
    private int resolveDurationSeconds(LocalDateTime startTime, LocalDateTime submitTime) {
        if (startTime == null || submitTime == null) {
            return 0;
        }
        long seconds = Duration.between(startTime, submitTime).getSeconds();
        return (int) Math.max(seconds, 0);
    }

    /**
     * 去空白并将空串转 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
