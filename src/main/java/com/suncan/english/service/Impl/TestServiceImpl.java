package com.suncan.english.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.constant.QuestionTypeConstant;
import com.suncan.english.dto.test.AnswerItemDTO;
import com.suncan.english.dto.test.SubmitAnswerDTO;
import com.suncan.english.dto.test.TestRecordQueryDTO;
import com.suncan.english.entity.PaperQuestion;
import com.suncan.english.entity.Question;
import com.suncan.english.entity.QuestionOption;
import com.suncan.english.entity.TestPaper;
import com.suncan.english.entity.UserTestAnswer;
import com.suncan.english.entity.UserTestRecord;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.mapper.PaperQuestionMapper;
import com.suncan.english.mapper.QuestionMapper;
import com.suncan.english.mapper.QuestionOptionMapper;
import com.suncan.english.mapper.TestPaperMapper;
import com.suncan.english.mapper.UserTestAnswerMapper;
import com.suncan.english.mapper.UserTestRecordMapper;
import com.suncan.english.service.TestService;
import com.suncan.english.service.UserService;
import com.suncan.english.vo.test.QuestionAnswerDetailVO;
import com.suncan.english.vo.test.QuestionOptionVO;
import com.suncan.english.vo.test.QuestionVO;
import com.suncan.english.vo.test.TestRecordDetailVO;
import com.suncan.english.vo.test.TestRecordItemVO;
import com.suncan.english.vo.test.TestRecordPageVO;
import com.suncan.english.vo.test.TestResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl implements TestService {

    private final TestPaperMapper testPaperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final UserTestRecordMapper userTestRecordMapper;
    private final UserTestAnswerMapper userTestAnswerMapper;
    private final UserService userService;

    public TestServiceImpl(TestPaperMapper testPaperMapper,
                           PaperQuestionMapper paperQuestionMapper,
                           QuestionMapper questionMapper,
                           QuestionOptionMapper questionOptionMapper,
                           UserTestRecordMapper userTestRecordMapper,
                           UserTestAnswerMapper userTestAnswerMapper,
                           UserService userService) {
        this.testPaperMapper = testPaperMapper;
        this.paperQuestionMapper = paperQuestionMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.userTestRecordMapper = userTestRecordMapper;
        this.userTestAnswerMapper = userTestAnswerMapper;
        this.userService = userService;
    }

    @Override
    public List<QuestionVO> getQuestions(Long paperId) {
        validatePaper(paperId);

        List<PaperQuestion> paperQuestionList = loadPaperQuestionRelationsWithCompat(paperId);
        List<Question> questionList = loadOrderedActiveQuestionsByRelations(paperQuestionList);
        if (questionList.isEmpty()) {
            throw new BusinessException("No questions in this paper");
        }

        List<Long> choiceQuestionIds = questionList.stream()
                .filter(item -> isChoiceQuestion(item.getQuestionType()))
                .map(Question::getId)
                .collect(Collectors.toList());

        Map<Long, List<QuestionOption>> optionMap = new LinkedHashMap<>();
        if (!choiceQuestionIds.isEmpty()) {
            List<QuestionOption> optionList = questionOptionMapper.selectList(
                    new LambdaQueryWrapper<QuestionOption>()
                            .in(QuestionOption::getQuestionId, choiceQuestionIds)
                            .orderByAsc(QuestionOption::getSortOrder, QuestionOption::getId)
            );
            optionMap = optionList.stream().collect(Collectors.groupingBy(QuestionOption::getQuestionId));
        }

        Map<Long, PaperQuestion> relationMap = toPaperQuestionMap(paperQuestionList);
        List<QuestionVO> result = new ArrayList<>();
        for (Question question : questionList) {
            validateQuestionType(question.getQuestionType());
            PaperQuestion relation = relationMap.get(question.getId());

            QuestionVO vo = new QuestionVO();
            vo.setQuestionId(question.getId());
            vo.setQuestionType(question.getQuestionType());
            vo.setSceneType(question.getSceneType());
            vo.setTitle(question.getTitle());
            vo.setContent(question.getContent());
            vo.setAudioUrl(question.getAudioUrl());
            vo.setScore(relation != null && relation.getScore() != null ? relation.getScore() : question.getScore());
            vo.setDifficulty(question.getDifficulty());
            vo.setSortOrder(relation != null && relation.getSortOrder() != null ? relation.getSortOrder() : question.getSortOrder());
            if (isChoiceQuestion(question.getQuestionType())) {
                vo.setOptions(toOptionVOList(optionMap.getOrDefault(question.getId(), Collections.emptyList())));
            } else {
                vo.setOptions(Collections.emptyList());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestResultVO submitAnswers(Long userId, SubmitAnswerDTO dto) {
        Long paperId = dto.getPaperId();
        validatePaper(paperId);

        List<PaperQuestion> paperQuestionList = loadPaperQuestionRelationsWithCompat(paperId);
        List<Question> questionList = loadOrderedActiveQuestionsByRelations(paperQuestionList);
        if (questionList.isEmpty()) {
            throw new BusinessException("No questions in this paper");
        }

        Map<Long, AnswerItemDTO> answerMap = normalizeAnswerMap(dto.getAnswers());
        Set<Long> paperQuestionIds = questionList.stream().map(Question::getId).collect(Collectors.toSet());
        for (Long questionId : answerMap.keySet()) {
            if (!paperQuestionIds.contains(questionId)) {
                throw new BusinessException("Invalid question ID in submit: " + questionId);
            }
        }

        int totalScore = 0;
        int correctCount = 0;
        LocalDateTime submitTime = LocalDateTime.now();
        int durationSeconds = resolveDurationSeconds(dto.getStartTime(), submitTime);

        Map<Long, PaperQuestion> relationMap = toPaperQuestionMap(paperQuestionList);
        List<UserTestAnswer> answerEntities = new ArrayList<>();
        for (Question question : questionList) {
            validateQuestionType(question.getQuestionType());

            AnswerItemDTO answerItem = answerMap.get(question.getId());
            String userAnswer = resolveUserAnswer(question.getQuestionType(), answerItem);
            String answerText = resolveAnswerText(question.getQuestionType(), answerItem);
            String audioAnswerUrl = resolveAudioAnswerUrl(question.getQuestionType(), answerItem);

            PaperQuestion relation = relationMap.get(question.getId());
            int questionScore = resolveQuestionScore(relation, question);
            boolean correct = false;
            int earnedScore = 0;

            if (!isSpeakingQuestion(question.getQuestionType())) {
                correct = isCorrect(userAnswer, question.getStandardAnswer());
                earnedScore = correct ? questionScore : 0;
            }

            if (correct) {
                correctCount++;
                totalScore += earnedScore;
            }

            UserTestAnswer answerEntity = new UserTestAnswer();
            answerEntity.setQuestionId(question.getId());
            answerEntity.setUserAnswer(userAnswer);
            answerEntity.setIsCorrect(correct ? 1 : 0);
            answerEntity.setScore(earnedScore);
            answerEntity.setAnswerText(answerText);
            answerEntity.setAudioAnswerUrl(audioAnswerUrl);
            answerEntity.setCreateTime(submitTime);
            answerEntities.add(answerEntity);
        }

        UserTestRecord record = new UserTestRecord();
        record.setUserId(userId);
        record.setPaperId(paperId);
        record.setTotalScore(totalScore);
        record.setCorrectCount(correctCount);
        record.setLevelResult(resolveLevelResult(totalScore));
        record.setStartTime(dto.getStartTime());
        record.setSubmitTime(submitTime);
        record.setDurationSeconds(durationSeconds);
        record.setCreateTime(submitTime);
        userTestRecordMapper.insert(record);

        for (UserTestAnswer answerEntity : answerEntities) {
            answerEntity.setRecordId(record.getId());
            userTestAnswerMapper.insert(answerEntity);
        }

        userService.updateEnglishLevel(userId, record.getLevelResult());
        return toResultVO(record);
    }

    @Override
    public TestResultVO latestResult(Long userId) {
        UserTestRecord latestRecord = userTestRecordMapper.selectOne(
                new LambdaQueryWrapper<UserTestRecord>()
                        .eq(UserTestRecord::getUserId, userId)
                        .orderByDesc(UserTestRecord::getId)
                        .last("limit 1")
        );
        if (latestRecord == null) {
            throw new BusinessException("No test record");
        }
        return toResultVO(latestRecord);
    }

    @Override
    public TestRecordPageVO queryRecordPage(Long userId, TestRecordQueryDTO queryDTO) {
        long current = queryDTO.getCurrent() == null || queryDTO.getCurrent() < 1 ? 1 : queryDTO.getCurrent();
        long size = queryDTO.getSize() == null || queryDTO.getSize() < 1 ? 10 : Math.min(queryDTO.getSize(), 50);

        List<Long> filterRecordIds = resolveRecordIdsByQuestionType(queryDTO.getQuestionType());
        if (queryDTO.getQuestionType() != null && filterRecordIds.isEmpty()) {
            return emptyPage(current, size);
        }

        LambdaQueryWrapper<UserTestRecord> countWrapper = buildRecordQueryWrapper(userId, queryDTO, filterRecordIds);
        long total = userTestRecordMapper.selectCount(countWrapper);
        if (total == 0) {
            return emptyPage(current, size);
        }

        long offset = (current - 1) * size;
        LambdaQueryWrapper<UserTestRecord> pageWrapper = buildRecordQueryWrapper(userId, queryDTO, filterRecordIds)
                .orderByDesc(UserTestRecord::getSubmitTime, UserTestRecord::getId)
                .last("limit " + offset + "," + size);
        List<UserTestRecord> pageRecords = userTestRecordMapper.selectList(pageWrapper);

        Map<Long, String> paperNameMap = loadPaperNameMap(pageRecords);
        List<TestRecordItemVO> recordVOList = pageRecords.stream().map(record -> {
            TestRecordItemVO itemVO = new TestRecordItemVO();
            itemVO.setRecordId(record.getId());
            itemVO.setPaperId(record.getPaperId());
            itemVO.setPaperName(paperNameMap.get(record.getPaperId()));
            itemVO.setTotalScore(record.getTotalScore());
            itemVO.setCorrectCount(record.getCorrectCount());
            itemVO.setLevelResult(record.getLevelResult());
            itemVO.setStartTime(record.getStartTime());
            itemVO.setSubmitTime(record.getSubmitTime());
            itemVO.setDurationSeconds(record.getDurationSeconds());
            return itemVO;
        }).collect(Collectors.toList());

        TestRecordPageVO pageVO = new TestRecordPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(recordVOList);
        return pageVO;
    }

    @Override
    public TestRecordDetailVO recordDetail(Long userId, Long recordId) {
        UserTestRecord record = userTestRecordMapper.selectOne(
                new LambdaQueryWrapper<UserTestRecord>()
                        .eq(UserTestRecord::getId, recordId)
                        .eq(UserTestRecord::getUserId, userId)
                        .last("limit 1")
        );
        if (record == null) {
            throw new BusinessException("Test record not found");
        }

        TestPaper paper = testPaperMapper.selectById(record.getPaperId());

        List<PaperQuestion> paperQuestionList = loadPaperQuestionRelationsWithCompat(record.getPaperId());
        List<Question> questionList = loadOrderedActiveQuestionsByRelations(paperQuestionList);
        List<Long> questionIds = questionList.stream().map(Question::getId).collect(Collectors.toList());

        Map<Long, UserTestAnswer> answerMap = new LinkedHashMap<>();
        if (!questionIds.isEmpty()) {
            List<UserTestAnswer> answerList = userTestAnswerMapper.selectList(
                    new LambdaQueryWrapper<UserTestAnswer>()
                            .eq(UserTestAnswer::getRecordId, recordId)
                            .in(UserTestAnswer::getQuestionId, questionIds)
            );
            answerMap = answerList.stream().collect(Collectors.toMap(
                    UserTestAnswer::getQuestionId,
                    item -> item,
                    (a, b) -> a,
                    LinkedHashMap::new
            ));
        }

        List<Long> choiceQuestionIds = questionList.stream()
                .filter(item -> isChoiceQuestion(item.getQuestionType()))
                .map(Question::getId)
                .collect(Collectors.toList());
        Map<Long, List<QuestionOption>> optionMap = new LinkedHashMap<>();
        if (!choiceQuestionIds.isEmpty()) {
            List<QuestionOption> optionList = questionOptionMapper.selectList(
                    new LambdaQueryWrapper<QuestionOption>()
                            .in(QuestionOption::getQuestionId, choiceQuestionIds)
                            .orderByAsc(QuestionOption::getSortOrder, QuestionOption::getId)
            );
            optionMap = optionList.stream().collect(Collectors.groupingBy(QuestionOption::getQuestionId));
        }

        List<QuestionAnswerDetailVO> questionAnswerList = new ArrayList<>();
        for (Question question : questionList) {
            validateQuestionType(question.getQuestionType());
            UserTestAnswer answer = answerMap.get(question.getId());

            QuestionAnswerDetailVO detailVO = new QuestionAnswerDetailVO();
            detailVO.setQuestionId(question.getId());
            detailVO.setQuestionType(question.getQuestionType());
            detailVO.setSceneType(question.getSceneType());
            detailVO.setTitle(question.getTitle());
            detailVO.setContent(question.getContent());
            detailVO.setAudioUrl(question.getAudioUrl());
            detailVO.setUserAnswer(answer == null ? null : answer.getUserAnswer());
            detailVO.setAnswerText(answer == null ? null : answer.getAnswerText());
            detailVO.setAudioAnswerUrl(answer == null ? null : answer.getAudioAnswerUrl());
            detailVO.setIsCorrect(answer == null ? 0 : answer.getIsCorrect());
            detailVO.setScore(answer == null ? 0 : answer.getScore());
            detailVO.setStandardAnswer(question.getStandardAnswer());
            detailVO.setAnalysis(question.getAnalysis());
            if (isChoiceQuestion(question.getQuestionType())) {
                detailVO.setOptionList(toOptionVOList(optionMap.getOrDefault(question.getId(), Collections.emptyList())));
            } else {
                detailVO.setOptionList(Collections.emptyList());
            }
            questionAnswerList.add(detailVO);
        }

        TestRecordDetailVO detailVO = new TestRecordDetailVO();
        detailVO.setRecordId(record.getId());
        detailVO.setPaperId(record.getPaperId());
        detailVO.setPaperName(paper == null ? null : paper.getPaperName());
        detailVO.setTotalScore(record.getTotalScore());
        detailVO.setCorrectCount(record.getCorrectCount());
        detailVO.setTotalCount(questionAnswerList.size());
        detailVO.setLevelResult(record.getLevelResult());
        detailVO.setStartTime(record.getStartTime());
        detailVO.setSubmitTime(record.getSubmitTime());
        detailVO.setDurationSeconds(record.getDurationSeconds());
        detailVO.setQuestionAnswerList(questionAnswerList);
        return detailVO;
    }

    private LambdaQueryWrapper<UserTestRecord> buildRecordQueryWrapper(Long userId,
                                                                       TestRecordQueryDTO queryDTO,
                                                                       List<Long> filterRecordIds) {
        LambdaQueryWrapper<UserTestRecord> wrapper = new LambdaQueryWrapper<UserTestRecord>()
                .eq(UserTestRecord::getUserId, userId)
                .eq(queryDTO.getPaperId() != null, UserTestRecord::getPaperId, queryDTO.getPaperId())
                .eq(hasText(queryDTO.getLevelResult()), UserTestRecord::getLevelResult, trimToNull(queryDTO.getLevelResult()))
                .in(filterRecordIds != null, UserTestRecord::getId, filterRecordIds);

        LocalDate startDate = queryDTO.getStartDate();
        if (startDate != null) {
            wrapper.ge(UserTestRecord::getSubmitTime, startDate.atStartOfDay());
        }
        LocalDate endDate = queryDTO.getEndDate();
        if (endDate != null) {
            wrapper.lt(UserTestRecord::getSubmitTime, endDate.plusDays(1).atStartOfDay());
        }
        return wrapper;
    }

    private List<Long> resolveRecordIdsByQuestionType(Integer questionType) {
        if (questionType == null) {
            return null;
        }
        validateQuestionType(questionType);

        List<Question> typedQuestions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getQuestionType, questionType)
                        .select(Question::getId)
        );
        if (typedQuestions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = typedQuestions.stream().map(Question::getId).collect(Collectors.toList());
        List<UserTestAnswer> answerList = userTestAnswerMapper.selectList(
                new LambdaQueryWrapper<UserTestAnswer>()
                        .in(UserTestAnswer::getQuestionId, questionIds)
                        .select(UserTestAnswer::getRecordId)
        );

        return answerList.stream().map(UserTestAnswer::getRecordId).distinct().collect(Collectors.toList());
    }

    private TestRecordPageVO emptyPage(long current, long size) {
        TestRecordPageVO pageVO = new TestRecordPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(0L);
        pageVO.setRecords(Collections.emptyList());
        return pageVO;
    }

    private Map<Long, String> loadPaperNameMap(List<UserTestRecord> recordList) {
        List<Long> paperIds = recordList.stream().map(UserTestRecord::getPaperId).distinct().collect(Collectors.toList());
        if (paperIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TestPaper> paperList = testPaperMapper.selectList(
                new LambdaQueryWrapper<TestPaper>()
                        .in(TestPaper::getId, paperIds)
                        .select(TestPaper::getId, TestPaper::getPaperName)
        );
        return paperList.stream().collect(Collectors.toMap(TestPaper::getId, TestPaper::getPaperName));
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

    // Smooth migration: prefer paper_question; fallback to question.paper_id only when relation table is empty.
    private List<PaperQuestion> loadPaperQuestionRelationsWithCompat(Long paperId) {
        List<PaperQuestion> relationList = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId)
                        .orderByAsc(PaperQuestion::getSortOrder, PaperQuestion::getId)
        );
        if (!relationList.isEmpty()) {
            return relationList;
        }

        List<Question> legacyQuestions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getPaperId, paperId)
                        .eq(Question::getStatus, 1)
                        .orderByAsc(Question::getSortOrder, Question::getId)
                        .select(Question::getId, Question::getScore, Question::getSortOrder)
        );

        List<PaperQuestion> legacyRelations = new ArrayList<>();
        for (Question question : legacyQuestions) {
            PaperQuestion paperQuestion = new PaperQuestion();
            paperQuestion.setPaperId(paperId);
            paperQuestion.setQuestionId(question.getId());
            paperQuestion.setScore(question.getScore());
            paperQuestion.setSortOrder(question.getSortOrder());
            legacyRelations.add(paperQuestion);
        }
        return legacyRelations;
    }

    private List<Question> loadOrderedActiveQuestionsByRelations(List<PaperQuestion> paperQuestionList) {
        if (paperQuestionList == null || paperQuestionList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = paperQuestionList.stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Question> questionEntities = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getId, questionIds)
                        .eq(Question::getStatus, 1)
        );
        if (questionEntities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = questionEntities.stream().collect(Collectors.toMap(Question::getId, item -> item));
        List<Question> orderedList = new ArrayList<>();
        for (PaperQuestion relation : paperQuestionList) {
            Question question = questionMap.get(relation.getQuestionId());
            if (question != null) {
                orderedList.add(question);
            }
        }
        return orderedList;
    }

    private Map<Long, PaperQuestion> toPaperQuestionMap(List<PaperQuestion> paperQuestionList) {
        Map<Long, PaperQuestion> relationMap = new LinkedHashMap<>();
        for (PaperQuestion relation : paperQuestionList) {
            if (relation == null || relation.getQuestionId() == null) {
                continue;
            }
            relationMap.putIfAbsent(relation.getQuestionId(), relation);
        }
        return relationMap;
    }

    private int resolveQuestionScore(PaperQuestion relation, Question question) {
        if (relation != null && relation.getScore() != null) {
            return relation.getScore();
        }
        return question.getScore() == null ? 0 : question.getScore();
    }

    private void validatePaper(Long paperId) {
        TestPaper paper = testPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("没有该试卷");
        }
        if (paper.getStatus() != null && paper.getStatus() == 0) {
            throw new BusinessException("试卷不能用");
        }
    }

    private Map<Long, AnswerItemDTO> normalizeAnswerMap(List<AnswerItemDTO> answers) {
        Map<Long, AnswerItemDTO> answerMap = new LinkedHashMap<>();
        if (answers == null) {
            return answerMap;
        }
        for (AnswerItemDTO item : answers) {
            if (item == null || item.getQuestionId() == null) {
                continue;
            }
            answerMap.put(item.getQuestionId(), item);
        }
        return answerMap;
    }

    private boolean isCorrect(String userAnswer, String standardAnswer) {
        String userNormalized = normalizeForCompare(userAnswer);
        String standardNormalized = normalizeForCompare(standardAnswer);
        if (userNormalized == null || standardNormalized == null) {
            return false;
        }
        return userNormalized.equals(standardNormalized);
    }

    private boolean isChoiceQuestion(Integer questionType) {
        return questionType != null
                && (questionType == QuestionTypeConstant.VOCABULARY_CHOICE
                || questionType == QuestionTypeConstant.LISTENING_CHOICE);
    }

    private boolean isBlankQuestion(Integer questionType) {
        return questionType != null && questionType == QuestionTypeConstant.GRAMMAR_CLOZE;
    }

    private boolean isSpeakingQuestion(Integer questionType) {
        return questionType != null && questionType == QuestionTypeConstant.SPEAKING_SUBJECTIVE;
    }

    private String resolveUserAnswer(Integer questionType, AnswerItemDTO answerItem) {
        if (answerItem == null) {
            return null;
        }
        if (isChoiceQuestion(questionType) || isBlankQuestion(questionType)) {
            return trimToNull(answerItem.getAnswer());
        }
        if (isSpeakingQuestion(questionType)) {
            return trimToNull(answerItem.getAnswerText());
        }
        return trimToNull(answerItem.getAnswer());
    }

    private String resolveAnswerText(Integer questionType, AnswerItemDTO answerItem) {
        if (answerItem == null) {
            return null;
        }
        if (isSpeakingQuestion(questionType)) {
            String answerText = trimToNull(answerItem.getAnswerText());
            return answerText != null ? answerText : trimToNull(answerItem.getAnswer());
        }
        return trimToNull(answerItem.getAnswerText());
    }

    private String resolveAudioAnswerUrl(Integer questionType, AnswerItemDTO answerItem) {
        if (answerItem == null) {
            return null;
        }
        return trimToNull(answerItem.getAudioAnswerUrl());
    }

    private String normalizeForCompare(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        return text.replace(" ", "").toUpperCase();
    }

    private String resolveLevelResult(int totalScore) {
        if (totalScore < 60) {
            return "初级";
        }
        if (totalScore < 80) {
            return "中级";
        }
        return "高级";
    }

    private int resolveDurationSeconds(LocalDateTime startTime, LocalDateTime submitTime) {
        if (startTime == null || submitTime == null) {
            return 0;
        }
        long seconds = Duration.between(startTime, submitTime).getSeconds();
        return (int) Math.max(seconds, 0);
    }

    private boolean hasText(String value) {
        return trimToNull(value) != null;
    }

    private void validateQuestionType(Integer questionType) {
        if (questionType == QuestionTypeConstant.VOCABULARY_CHOICE
                || questionType == QuestionTypeConstant.GRAMMAR_CLOZE
                || questionType == QuestionTypeConstant.LISTENING_CHOICE
                || questionType == QuestionTypeConstant.SPEAKING_SUBJECTIVE) {
            return;
        }
        throw new BusinessException("Invalid question type");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private TestResultVO toResultVO(UserTestRecord record) {
        TestResultVO vo = new TestResultVO();
        vo.setRecordId(record.getId());
        vo.setPaperId(record.getPaperId());
        vo.setTotalScore(record.getTotalScore());
        vo.setCorrectCount(record.getCorrectCount());
        vo.setLevelResult(record.getLevelResult());
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        vo.setDurationSeconds(record.getDurationSeconds());
        return vo;
    }
}