package com.suncan.english.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.constant.QuestionTypeConstant;
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

/**
 * 语言能力评估业务实现。
 *
 * 说明：
 * 1. 该服务是“跨多表聚合业务”，核心职责是组装试题、提交判分、查询记录详情。
 * 2. 因为涉及 question / question_option / user_test_record / user_test_answer 等多表联动，
 *    所以这里不强制继承 MyBatis-Plus 的 ServiceImpl（它更适合单表实体的通用 CRUD）。
 * 3. 本类按题型分流处理，确保“是否返回选项、如何判分、详情如何回显”都与题型一致。
 */
@Service
public class TestServiceImpl implements TestService {

    /** 试卷表 Mapper：校验试卷状态、查询试卷名称。 */
    private final TestPaperMapper testPaperMapper;
    /** 题目表 Mapper：读取题目主体信息。 */
    private final QuestionMapper questionMapper;
    /** 题目选项表 Mapper：仅在选择题场景读取。 */
    private final QuestionOptionMapper questionOptionMapper;
    /** 测试记录表 Mapper：保存整次测试汇总。 */
    private final UserTestRecordMapper userTestRecordMapper;
    /** 测试答案表 Mapper：保存每题作答明细。 */
    private final UserTestAnswerMapper userTestAnswerMapper;
    /** 用户服务：测试后同步更新用户等级。 */
    private final UserService userService;

    public TestServiceImpl(TestPaperMapper testPaperMapper,
                           QuestionMapper questionMapper,
                           QuestionOptionMapper questionOptionMapper,
                           UserTestRecordMapper userTestRecordMapper,
                           UserTestAnswerMapper userTestAnswerMapper,
                           UserService userService) {
        this.testPaperMapper = testPaperMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.userTestRecordMapper = userTestRecordMapper;
        this.userTestAnswerMapper = userTestAnswerMapper;
        this.userService = userService;
    }

    /**
     * 查询试卷题目。
     *
     * 关键规则：
     * 1. 只有选择题（词汇单选、听力选择）返回 options。
     * 2. 填空题和口语题返回空 options，防止前端误渲染为选择题。
     */
    @Override
    public List<QuestionVO> getQuestions(Long paperId) {
        // 1) 校验试卷存在且启用。
        validatePaper(paperId);

        // 2) 查询试卷下启用题目，按 sortOrder + id 排序，保证前后端顺序稳定。
        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getPaperId, paperId)
                        .eq(Question::getStatus, 1)
                        .orderByAsc(Question::getSortOrder, Question::getId)
        );
        if (questionList == null || questionList.isEmpty()) {
            throw new BusinessException("该试卷暂无题目");
        }

        // 3) 只加载选择题选项，非选择题不查选项。
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

        // 4) 组装返回 VO。
        List<QuestionVO> result = new ArrayList<>();
        for (Question question : questionList) {
            validateQuestionType(question.getQuestionType());

            QuestionVO vo = new QuestionVO();
            vo.setQuestionId(question.getId());
            vo.setQuestionType(question.getQuestionType());
            vo.setSceneType(question.getSceneType());
            vo.setTitle(question.getTitle());
            vo.setContent(question.getContent());
            vo.setAudioUrl(question.getAudioUrl());
            vo.setScore(question.getScore());
            vo.setDifficulty(question.getDifficulty());
            vo.setSortOrder(question.getSortOrder());
            if (isChoiceQuestion(question.getQuestionType())) {
                vo.setOptions(toOptionVOList(optionMap.getOrDefault(question.getId(), Collections.emptyList())));
            } else {
                vo.setOptions(Collections.emptyList());
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 提交答案并判分。
     *
     * 题型判分策略：
     * 1. 词汇单选题：answer 与 standardAnswer 比较。
     * 2. 语法填空题：answer 文本与 standardAnswer 比较。
     * 3. 听力选择题：answer 与 standardAnswer 比较。
     * 4. 口语主观题：先保存 answerText/audioAnswerUrl，暂不自动评分（预留扩展）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestResultVO submitAnswers(Long userId, SubmitAnswerDTO dto) {
        Long paperId = dto.getPaperId();
        validatePaper(paperId);

        // 1) 查询当前试卷可答题目。
        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getPaperId, paperId)
                        .eq(Question::getStatus, 1)
                        .orderByAsc(Question::getSortOrder, Question::getId)
        );
        if (questionList == null || questionList.isEmpty()) {
            throw new BusinessException("该试卷暂无题目");
        }

        // 2) 答案归一化：List -> Map，便于按题 id 快速取值。
        Map<Long, AnswerItemDTO> answerMap = normalizeAnswerMap(dto.getAnswers());

        // 3) 安全校验：提交题目必须属于当前试卷。
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
            validateQuestionType(question.getQuestionType());

            AnswerItemDTO answerItem = answerMap.get(question.getId());

            // 不同题型读取不同作答字段。
            String userAnswer = resolveUserAnswer(question.getQuestionType(), answerItem);
            String answerText = resolveAnswerText(question.getQuestionType(), answerItem);
            String audioAnswerUrl = resolveAudioAnswerUrl(question.getQuestionType(), answerItem);

            int questionScore = question.getScore() == null ? 0 : question.getScore();
            boolean correct = false;
            int earnedScore = 0;

            // 口语主观题暂不自动评分。
            // 原因：完整口语评分通常依赖 ASR、流利度、语义等能力，当前阶段先打通存储和回显。
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

        // 4) 保存本次测试汇总记录。
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

        // 5) 保存每题作答明细。
        for (UserTestAnswer answerEntity : answerEntities) {
            answerEntity.setRecordId(record.getId());
            userTestAnswerMapper.insert(answerEntity);
        }

        // 6) 同步更新用户当前英语等级。
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
            throw new BusinessException("暂无测试记录");
        }
        return toResultVO(latestRecord);
    }

    @Override
    public TestRecordPageVO queryRecordPage(Long userId, TestRecordQueryDTO queryDTO) {
        // 分页参数兜底和上限保护。
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

    /**
     * 查询测试记录详情。
     *
     * 关键规则：
     * 1. 选择题详情返回 optionList。
     * 2. 填空题/口语题详情返回空 optionList。
     */
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

        // 1) 查询本次记录每题作答。
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

        // 2) 只为选择题查询选项。
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

        // 3) 组装详情。
        List<QuestionAnswerDetailVO> questionAnswerList = new ArrayList<>();
        for (Question question : questionList) {
            validateQuestionType(question.getQuestionType());
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

    /** 构建记录分页查询条件。 */
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

    /** 按题型筛选记录 ID（用于分页筛选条件）。 */
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

    /** 返回空分页对象。 */
    private TestRecordPageVO emptyPage(long current, long size) {
        TestRecordPageVO pageVO = new TestRecordPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(0L);
        pageVO.setRecords(Collections.emptyList());
        return pageVO;
    }

    /** 批量加载试卷名称，避免 N+1 查询。 */
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

    /** 选项实体转 VO。 */
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

    /** 校验试卷存在且启用。 */
    private void validatePaper(Long paperId) {
        TestPaper paper = testPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (paper.getStatus() != null && paper.getStatus() == 0) {
            throw new BusinessException("试卷未启用");
        }
    }

    /** 答案列表归一化为 Map（同 questionId 后者覆盖前者）。 */
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

    /** 客观题判分比较：忽略大小写和多余空格。 */
    private boolean isCorrect(String userAnswer, String standardAnswer) {
        String userNormalized = normalizeForCompare(userAnswer);
        String standardNormalized = normalizeForCompare(standardAnswer);
        if (userNormalized == null || standardNormalized == null) {
            return false;
        }
        return userNormalized.equals(standardNormalized);
    }

    /** 是否选择题：词汇单选、听力选择。 */
    private boolean isChoiceQuestion(Integer questionType) {
        return questionType != null
                && (questionType == QuestionTypeConstant.VOCABULARY_CHOICE
                || questionType == QuestionTypeConstant.LISTENING_CHOICE);
    }

    /** 是否填空题：语法填空。 */
    private boolean isBlankQuestion(Integer questionType) {
        return questionType != null && questionType == QuestionTypeConstant.GRAMMAR_CLOZE;
    }

    /** 是否口语主观题。 */
    private boolean isSpeakingQuestion(Integer questionType) {
        return questionType != null && questionType == QuestionTypeConstant.SPEAKING_SUBJECTIVE;
    }

    /**
     * 解析 userAnswer 字段：
     * 1/2/3 题型直接读 answer；4 题型用 answerText 兜底，便于详情统一回显。
     */
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

    /**
     * 解析 answerText 字段：
     * 口语题优先取 answerText，同时兼容前端暂时只传 answer 的情况。
     */
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

    /** 解析语音作答地址。 */
    private String resolveAudioAnswerUrl(Integer questionType, AnswerItemDTO answerItem) {
        if (answerItem == null) {
            return null;
        }
        return trimToNull(answerItem.getAudioAnswerUrl());
    }

    /** 字符串标准化：去前后空格、移除中间空格、统一大写。 */
    private String normalizeForCompare(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        return text.replace(" ", "").toUpperCase();
    }

    /** 根据总分换算等级。 */
    private String resolveLevelResult(int totalScore) {
        if (totalScore < 60) {
            return "初级";
        }
        if (totalScore < 80) {
            return "中级";
        }
        return "高级";
    }

    /** 计算答题时长（秒），若时间异常则返回 0。 */
    private int resolveDurationSeconds(LocalDateTime startTime, LocalDateTime submitTime) {
        if (startTime == null || submitTime == null) {
            return 0;
        }
        long seconds = Duration.between(startTime, submitTime).getSeconds();
        return (int) Math.max(seconds, 0);
    }

    /** 是否有有效文本。 */
    private boolean hasText(String value) {
        return trimToNull(value) != null;
    }

    /** 统一题型白名单校验，避免魔法值散落在业务逻辑中。 */
    private void validateQuestionType(Integer questionType) {
        if (questionType == QuestionTypeConstant.VOCABULARY_CHOICE
                || questionType == QuestionTypeConstant.GRAMMAR_CLOZE
                || questionType == QuestionTypeConstant.LISTENING_CHOICE
                || questionType == QuestionTypeConstant.SPEAKING_SUBJECTIVE) {
            return;
        }
        throw new BusinessException("题目类型不合法");
    }

    /** 去前后空格，空字符串转 null。 */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** 记录实体转提交结果 VO。 */
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
