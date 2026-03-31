package com.suncan.english.module.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.suncan.english.module.learning.entity.StudyPlan;
import com.suncan.english.module.learning.entity.StudyTask;
import com.suncan.english.module.learning.mapper.StudyPlanMapper;
import com.suncan.english.module.learning.mapper.StudyTaskMapper;
import com.suncan.english.module.practice.dto.PracticeAnswerItemDTO;
import com.suncan.english.module.practice.dto.PracticeRecordQueryDTO;
import com.suncan.english.module.practice.dto.SubmitPracticeDTO;
import com.suncan.english.module.practice.entity.UserPracticeAnswer;
import com.suncan.english.module.practice.entity.UserPracticeRecord;
import com.suncan.english.module.practice.mapper.UserPracticeAnswerMapper;
import com.suncan.english.module.practice.mapper.UserPracticeRecordMapper;
import com.suncan.english.module.practice.service.PracticeService;
import com.suncan.english.module.practice.vo.PracticeQuestionAnswerDetailVO;
import com.suncan.english.module.practice.vo.PracticeRecordDetailVO;
import com.suncan.english.module.practice.vo.PracticeRecordItemVO;
import com.suncan.english.module.practice.vo.PracticeRecordPageVO;
import com.suncan.english.module.practice.vo.PracticeSubmitResultVO;
import com.suncan.english.module.practice.vo.PracticeTaskVO;
import com.suncan.english.module.questionbank.entity.Question;
import com.suncan.english.module.questionbank.entity.QuestionOption;
import com.suncan.english.module.questionbank.mapper.QuestionMapper;
import com.suncan.english.module.questionbank.mapper.QuestionOptionMapper;
import com.suncan.english.module.reward.service.BadgeService;
import com.suncan.english.module.reward.service.PointsService;
import com.suncan.english.module.test.vo.QuestionOptionVO;
import com.suncan.english.module.test.vo.QuestionVO;
import com.suncan.english.module.user.entity.User;
import com.suncan.english.module.user.mapper.UserMapper;
import com.suncan.english.shared.constant.PointsConstant;
import com.suncan.english.shared.enums.EnglishLevelEnum;
import com.suncan.english.shared.enums.QuestionDifficultyEnum;
import com.suncan.english.shared.enums.QuestionTypeEnum;
import com.suncan.english.shared.enums.SceneTypeEnum;
import com.suncan.english.shared.enums.StudyTaskStatusEnum;
import com.suncan.english.shared.enums.TaskTypeEnum;
import com.suncan.english.shared.exception.BusinessException;
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

@Service
public class PracticeServiceImpl implements PracticeService {

    private static final int PRACTICE_RECORD_STATUS_SUBMITTED = 1;

    private final StudyTaskMapper studyTaskMapper;
    private final StudyPlanMapper studyPlanMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final UserPracticeRecordMapper userPracticeRecordMapper;
    private final UserPracticeAnswerMapper userPracticeAnswerMapper;
    private final PointsService pointsService;
    private final BadgeService badgeService;

    public PracticeServiceImpl(StudyTaskMapper studyTaskMapper,
                               StudyPlanMapper studyPlanMapper,
                               UserMapper userMapper,
                               QuestionMapper questionMapper,
                               QuestionOptionMapper questionOptionMapper,
                               UserPracticeRecordMapper userPracticeRecordMapper,
                               UserPracticeAnswerMapper userPracticeAnswerMapper,
                               PointsService pointsService,
                               BadgeService badgeService) {
        this.studyTaskMapper = studyTaskMapper;
        this.studyPlanMapper = studyPlanMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.userPracticeRecordMapper = userPracticeRecordMapper;
        this.userPracticeAnswerMapper = userPracticeAnswerMapper;
        this.pointsService = pointsService;
        this.badgeService = badgeService;
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeSubmitResultVO submitPractice(Long userId, SubmitPracticeDTO dto) {
        StudyTask task = requireOwnedTask(userId, dto.getTaskId());
        if (StudyTaskStatusEnum.DONE.getCode().equals(task.getStatus())) {
            throw new BusinessException("当前任务已完成，请勿重复提交");
        }

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
            throw new BusinessException("答案不能为空");
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
            if (QuestionTypeEnum.isSpeaking(questionType)) {
                correct = true;
                earnedScore = questionScore;
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

        pointsService.addPoints(userId, PointsConstant.PRACTICE_POINTS);
        badgeService.checkAndGrantBadges(userId);

        PracticeSubmitResultVO vo = new PracticeSubmitResultVO();
        vo.setRecordId(record.getId());
        vo.setTaskId(task.getId());
        vo.setTotalCount(totalCount);
        vo.setCorrectCount(correctCount);
        vo.setTotalScore(totalScore);
        vo.setDurationSeconds(durationSeconds);
        return vo;
    }

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
                detailVO.setOptionList(Collections.emptyList());
            }
            detailList.add(detailVO);
        }

        return toRecordDetailVO(record, detailList);
    }

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

    private LambdaQueryWrapper<UserPracticeRecord> buildRecordListWrapper(Long userId, PracticeRecordQueryDTO queryDTO) {
        return new LambdaQueryWrapper<UserPracticeRecord>()
                .eq(UserPracticeRecord::getUserId, userId)
                .eq(queryDTO.getPlanId() != null, UserPracticeRecord::getPlanId, queryDTO.getPlanId())
                .eq(queryDTO.getTaskId() != null, UserPracticeRecord::getTaskId, queryDTO.getTaskId());
    }

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

    private UserPracticeRecord requireOwnedRecord(Long userId, Long recordId) {
        UserPracticeRecord record = userPracticeRecordMapper.selectOne(
                new LambdaQueryWrapper<UserPracticeRecord>()
                        .eq(UserPracticeRecord::getId, recordId)
                        .eq(UserPracticeRecord::getUserId, userId)
                        .last("limit 1")
        );
        if (record == null) {
            throw new BusinessException("练习记录不存在或无权限");
        }
        return record;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private List<Question> loadCandidateQuestionsByRule(StudyTask task, Integer englishLevel) {
        if (task.getQuestionType() == null) {
            return Collections.emptyList();
        }
        QuestionDifficultyEnum difficultyEnum = resolveDifficultyByUserLevel(englishLevel);
        if (difficultyEnum == null) {
            return Collections.emptyList();
        }

        List<Question> strictMatched = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getStatus, 1)
                        .eq(Question::getQuestionType, task.getQuestionType())
                        .eq(task.getSceneType() != null, Question::getSceneType, task.getSceneType())
                        .eq(Question::getDifficulty, difficultyEnum.getCode())
        );
        if (!strictMatched.isEmpty()) {
            return strictMatched;
        }

        List<Question> sceneMatched = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getStatus, 1)
                        .eq(Question::getQuestionType, task.getQuestionType())
                        .eq(task.getSceneType() != null, Question::getSceneType, task.getSceneType())
        );
        if (!sceneMatched.isEmpty()) {
            return sceneMatched;
        }

        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getStatus, 1)
                        .eq(Question::getQuestionType, task.getQuestionType())
        );
    }

    private List<Question> pickQuestionsForDisplay(List<Question> candidateList, int expectCount) {
        if (candidateList == null || candidateList.isEmpty() || expectCount <= 0) {
            return Collections.emptyList();
        }
        List<Question> shuffled = new ArrayList<>(candidateList);
        Collections.shuffle(shuffled);
        return shuffled.size() <= expectCount ? shuffled : new ArrayList<>(shuffled.subList(0, expectCount));
    }

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

    private QuestionDifficultyEnum resolveDifficultyByUserLevel(Integer englishLevel) {
        if (EnglishLevelEnum.INTERMEDIATE.getCode().equals(englishLevel)) {
            return QuestionDifficultyEnum.MEDIUM;
        }
        if (EnglishLevelEnum.ADVANCED.getCode().equals(englishLevel)) {
            return QuestionDifficultyEnum.HARD;
        }
        return QuestionDifficultyEnum.EASY;
    }

    private Map<Long, List<QuestionOption>> loadOptionMap(List<Question> questionList) {
        Set<Long> questionIds = questionList.stream()
                .filter(item -> QuestionTypeEnum.isChoice(item.getQuestionType()))
                .map(Question::getId)
                .collect(Collectors.toSet());
        return loadOptionMapByQuestionIds(questionIds);
    }

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

    private boolean isTextEqualIgnoreCaseAndSpace(String left, String right) {
        String leftNormalized = normalizeForCompare(left);
        String rightNormalized = normalizeForCompare(right);
        if (leftNormalized == null || rightNormalized == null) {
            return false;
        }
        return leftNormalized.equals(rightNormalized);
    }

    private String normalizeForCompare(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        return text.replace(" ", "").toUpperCase();
    }

    private int resolveDurationSeconds(LocalDateTime startTime, LocalDateTime submitTime) {
        if (startTime == null || submitTime == null) {
            return 0;
        }
        long seconds = Duration.between(startTime, submitTime).getSeconds();
        return (int) Math.max(seconds, 0);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

