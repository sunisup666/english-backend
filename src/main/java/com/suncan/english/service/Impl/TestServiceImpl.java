package com.suncan.english.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.dto.test.AnswerItemDTO;
import com.suncan.english.dto.test.SubmitAnswerDTO;
import com.suncan.english.dto.test.TestRecordQueryDTO;
import com.suncan.english.entity.Question;
import com.suncan.english.entity.QuestionOption;
import com.suncan.english.entity.TestPaper;
import com.suncan.english.entity.UserTestAnswer;
import com.suncan.english.entity.UserTestRecord;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.mapper.QuestionMapper;
import com.suncan.english.mapper.QuestionOptionMapper;
import com.suncan.english.mapper.TestPaperMapper;
import com.suncan.english.mapper.UserTestAnswerMapper;
import com.suncan.english.mapper.UserTestRecordMapper;
import com.suncan.english.service.TestService;
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

/**
 * 语言能力评估业务实现。
 */
@Service
public class TestServiceImpl implements TestService {

    private final TestPaperMapper testPaperMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final UserTestRecordMapper userTestRecordMapper;
    private final UserTestAnswerMapper userTestAnswerMapper;

    public TestServiceImpl(TestPaperMapper testPaperMapper,
                           QuestionMapper questionMapper,
                           QuestionOptionMapper questionOptionMapper,
                           UserTestRecordMapper userTestRecordMapper,
                           UserTestAnswerMapper userTestAnswerMapper) {
        this.testPaperMapper = testPaperMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.userTestRecordMapper = userTestRecordMapper;
        this.userTestAnswerMapper = userTestAnswerMapper;
    }

    @Override
    public List<QuestionVO> getQuestions(Long paperId) {
        validatePaper(paperId);

        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getPaperId, paperId)
                        .eq(Question::getStatus, 1)
                        .orderByAsc(Question::getSortOrder, Question::getId)
        );
        if (questionList == null || questionList.isEmpty()) {
            throw new BusinessException("该试卷暂无题目");
        }

        List<Long> questionIds = questionList.stream().map(Question::getId).collect(Collectors.toList());
        List<QuestionOption> optionList = questionOptionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>()
                        .in(QuestionOption::getQuestionId, questionIds)
                        .orderByAsc(QuestionOption::getSortOrder, QuestionOption::getId)
        );
        Map<Long, List<QuestionOption>> optionMap = optionList.stream()
                .collect(Collectors.groupingBy(QuestionOption::getQuestionId));

        List<QuestionVO> result = new ArrayList<>();
        for (Question question : questionList) {
            QuestionVO vo = new QuestionVO();
            vo.setQuestionId(question.getId());
            vo.setQuestionType(question.getQuestionType());
            vo.setTitle(question.getTitle());
            vo.setContent(question.getContent());
            vo.setAudioUrl(question.getAudioUrl());
            vo.setScore(question.getScore());
            vo.setDifficulty(question.getDifficulty());
            vo.setSortOrder(question.getSortOrder());
            vo.setOptions(toOptionVOList(optionMap.getOrDefault(question.getId(), Collections.emptyList())));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestResultVO submitAnswers(Long userId, SubmitAnswerDTO dto) {
        Long paperId = dto.getPaperId();
        validatePaper(paperId);

        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getPaperId, paperId)
                        .eq(Question::getStatus, 1)
                        .orderByAsc(Question::getSortOrder, Question::getId)
        );
        if (questionList == null || questionList.isEmpty()) {
            throw new BusinessException("该试卷暂无题目");
        }

        Map<Long, AnswerItemDTO> answerMap = normalizeAnswerMap(dto.getAnswers());
        Set<Long> paperQuestionIds = questionList.stream().map(Question::getId).collect(Collectors.toSet());
        for (Long questionId : answerMap.keySet()) {
            if (!paperQuestionIds.contains(questionId)) {
                throw new BusinessException("提交答案中包含无效题目ID: " + questionId);
            }
        }

        int totalScore = 0;
        int correctCount = 0;
        LocalDateTime submitTime = LocalDateTime.now();
        int durationSeconds = resolveDurationSeconds(dto.getStartTime(), submitTime);

        List<UserTestAnswer> answerEntities = new ArrayList<>();
        for (Question question : questionList) {
            AnswerItemDTO answerItem = answerMap.get(question.getId());
            String userAnswer = answerItem == null ? null : trimToNull(answerItem.getAnswer());

            boolean correct = isCorrect(userAnswer, question.getStandardAnswer());
            int questionScore = question.getScore() == null ? 0 : question.getScore();
            int earnedScore = correct ? questionScore : 0;

            if (correct) {
                correctCount++;
                totalScore += earnedScore;
            }

            UserTestAnswer answerEntity = new UserTestAnswer();
            answerEntity.setQuestionId(question.getId());
            answerEntity.setUserAnswer(userAnswer);
            answerEntity.setIsCorrect(correct ? 1 : 0);
            answerEntity.setScore(earnedScore);
            answerEntity.setAnswerText(answerItem == null ? null : trimToNull(answerItem.getAnswerText()));
            answerEntity.setAudioAnswerUrl(answerItem == null ? null : trimToNull(answerItem.getAudioAnswerUrl()));
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
            throw new BusinessException("暂无测试记录");
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
            throw new BusinessException("测试记录不存在");
        }

        TestPaper paper = testPaperMapper.selectById(record.getPaperId());

        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getPaperId, record.getPaperId())
                        .eq(Question::getStatus, 1)
                        .orderByAsc(Question::getSortOrder, Question::getId)
        );
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

        Map<Long, List<QuestionOption>> optionMap = new LinkedHashMap<>();
        if (!questionIds.isEmpty()) {
            List<QuestionOption> optionList = questionOptionMapper.selectList(
                    new LambdaQueryWrapper<QuestionOption>()
                            .in(QuestionOption::getQuestionId, questionIds)
                            .orderByAsc(QuestionOption::getSortOrder, QuestionOption::getId)
            );
            optionMap = optionList.stream().collect(Collectors.groupingBy(QuestionOption::getQuestionId));
        }

        List<QuestionAnswerDetailVO> questionAnswerList = new ArrayList<>();
        for (Question question : questionList) {
            UserTestAnswer answer = answerMap.get(question.getId());

            QuestionAnswerDetailVO detailVO = new QuestionAnswerDetailVO();
            detailVO.setQuestionId(question.getId());
            detailVO.setQuestionType(question.getQuestionType());
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
            detailVO.setOptionList(toOptionVOList(optionMap.getOrDefault(question.getId(), Collections.emptyList())));
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

    private List<Long> resolveRecordIdsByQuestionType(String questionType) {
        String normalizedType = trimToNull(questionType);
        if (normalizedType == null) {
            return null;
        }

        List<Question> typedQuestions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getQuestionType, normalizedType)
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

    private void validatePaper(Long paperId) {
        TestPaper paper = testPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (paper.getStatus() != null && paper.getStatus() == 0) {
            throw new BusinessException("试卷未启用");
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