package com.suncan.english.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.module.admin.dto.AdminQuestionOptionDTO;
import com.suncan.english.module.admin.dto.AdminQuestionQueryDTO;
import com.suncan.english.module.admin.dto.AdminQuestionSaveDTO;
import com.suncan.english.module.admin.service.AdminQuestionService;
import com.suncan.english.module.admin.vo.AdminQuestionOptionVO;
import com.suncan.english.module.admin.vo.AdminQuestionPageVO;
import com.suncan.english.module.admin.vo.AdminQuestionVO;
import com.suncan.english.module.questionbank.entity.PaperQuestion;
import com.suncan.english.module.questionbank.entity.Question;
import com.suncan.english.module.questionbank.entity.QuestionOption;
import com.suncan.english.module.questionbank.mapper.PaperQuestionMapper;
import com.suncan.english.module.questionbank.mapper.QuestionMapper;
import com.suncan.english.module.questionbank.mapper.QuestionOptionMapper;
import com.suncan.english.shared.enums.QuestionDifficultyEnum;
import com.suncan.english.shared.enums.QuestionTypeEnum;
import com.suncan.english.shared.enums.SceneTypeEnum;
import com.suncan.english.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminQuestionServiceImpl implements AdminQuestionService {

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final PaperQuestionMapper paperQuestionMapper;

    public AdminQuestionServiceImpl(QuestionMapper questionMapper,
                                    QuestionOptionMapper questionOptionMapper,
                                    PaperQuestionMapper paperQuestionMapper) {
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.paperQuestionMapper = paperQuestionMapper;
    }

    @Override
    public AdminQuestionPageVO page(AdminQuestionQueryDTO queryDTO) {
        long current = queryDTO == null || queryDTO.getCurrent() == null || queryDTO.getCurrent() < 1 ? 1 : queryDTO.getCurrent();
        long size = queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() < 1 ? 10 : Math.min(queryDTO.getSize(), 50);
        validateQuery(queryDTO);

        LambdaQueryWrapper<Question> countWrapper = buildPageWrapper(queryDTO);
        Long total = questionMapper.selectCount(countWrapper);
        if (total == null || total == 0L) {
            AdminQuestionPageVO empty = new AdminQuestionPageVO();
            empty.setCurrent(current);
            empty.setSize(size);
            empty.setTotal(0L);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        long offset = (current - 1) * size;
        List<Question> questionList = questionMapper.selectList(
                buildPageWrapper(queryDTO)
                        .orderByDesc(Question::getUpdateTime)
                        .orderByDesc(Question::getId)
                        .last("limit " + offset + "," + size)
        );

        Map<Long, List<QuestionOption>> optionMap = loadOptionMap(
                questionList.stream().map(Question::getId).collect(Collectors.toList())
        );
        List<AdminQuestionVO> records = questionList.stream()
                .map(item -> toQuestionVO(item, optionMap.get(item.getId())))
                .collect(Collectors.toList());

        AdminQuestionPageVO pageVO = new AdminQuestionPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    @Override
    public AdminQuestionVO detail(Long id) {
        if (id == null) {
            throw new BusinessException("题目ID不能为空");
        }
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        List<QuestionOption> options = questionOptionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, id)
                        .orderByAsc(QuestionOption::getSortOrder, QuestionOption::getId)
        );
        return toQuestionVO(question, options);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AdminQuestionSaveDTO dto) {
        validateSaveDTO(dto);

        LocalDateTime now = LocalDateTime.now();
        Question question = new Question();
        fillQuestion(question, dto, now, true);
        questionMapper.insert(question);
        replaceOptions(question.getId(), dto.getQuestionType(), dto.getOptions(), now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AdminQuestionSaveDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException("修改题目时题目ID不能为空");
        }
        Question existed = questionMapper.selectById(dto.getId());
        if (existed == null) {
            throw new BusinessException("题目不存在");
        }

        validateSaveDTO(dto);

        LocalDateTime now = LocalDateTime.now();
        fillQuestion(existed, dto, now, false);
        questionMapper.updateById(existed);
        replaceOptions(existed.getId(), dto.getQuestionType(), dto.getOptions(), now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("题目ID不能为空");
        }
        Question existed = questionMapper.selectById(id);
        if (existed == null) {
            throw new BusinessException("题目不存在");
        }

        questionOptionMapper.delete(new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, id));
        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getQuestionId, id));
        questionMapper.deleteById(id);
    }

    private void validateQuery(AdminQuestionQueryDTO queryDTO) {
        if (queryDTO == null) {
            return;
        }
        validateOptionalQuestionType(queryDTO.getQuestionType());
        validateOptionalSceneType(queryDTO.getSceneType());
        validateOptionalDifficulty(queryDTO.getDifficulty());
        validateOptionalStatus(queryDTO.getStatus());
    }

    private LambdaQueryWrapper<Question> buildPageWrapper(AdminQuestionQueryDTO queryDTO) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO == null) {
            return wrapper;
        }
        String keyword = trimToNull(queryDTO.getKeyword());
        wrapper.eq(queryDTO.getQuestionType() != null, Question::getQuestionType, queryDTO.getQuestionType())
                .eq(queryDTO.getSceneType() != null, Question::getSceneType, queryDTO.getSceneType())
                .eq(queryDTO.getDifficulty() != null, Question::getDifficulty, queryDTO.getDifficulty())
                .eq(queryDTO.getStatus() != null, Question::getStatus, queryDTO.getStatus())
                .like(keyword != null, Question::getTitle, keyword);
        return wrapper;
    }

    private void validateSaveDTO(AdminQuestionSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException("题目参数不能为空");
        }
        validateQuestionType(dto.getQuestionType());
        validateSceneType(dto.getSceneType());
        validateDifficulty(dto.getDifficulty());
        validateStatus(dto.getStatus());
        requireText(dto.getTitle(), "题目标题不能为空");
        requireText(dto.getContent(), "题干内容不能为空");
        requireText(dto.getStandardAnswer(), "标准答案不能为空");
        if (dto.getScore() == null || dto.getScore() < 0) {
            throw new BusinessException("题目分值不能为空且不能小于0");
        }
        if (QuestionTypeEnum.isChoice(dto.getQuestionType())) {
            validateChoiceOptions(dto.getOptions());
        }
    }

    private void validateChoiceOptions(List<AdminQuestionOptionDTO> options) {
        if (options == null || options.isEmpty()) {
            throw new BusinessException("选择题必须至少包含一个选项");
        }
        boolean hasCorrectOption = false;
        for (AdminQuestionOptionDTO option : options) {
            if (option == null) {
                continue;
            }
            requireText(option.getOptionLabel(), "选项标签不能为空");
            requireText(option.getOptionContent(), "选项内容不能为空");
            if (Objects.equals(option.getIsCorrect(), 1)) {
                hasCorrectOption = true;
            }
        }
        if (!hasCorrectOption) {
            throw new BusinessException("选择题必须至少有一个正确选项");
        }
    }

    private void fillQuestion(Question question, AdminQuestionSaveDTO dto, LocalDateTime now, boolean creating) {
        question.setQuestionType(dto.getQuestionType());
        question.setSceneType(dto.getSceneType());
        question.setTitle(dto.getTitle().trim());
        question.setContent(dto.getContent().trim());
        question.setAudioUrl(trimToNull(dto.getAudioUrl()));
        question.setStandardAnswer(dto.getStandardAnswer().trim());
        question.setScore(dto.getScore());
        question.setDifficulty(dto.getDifficulty());
        question.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        question.setAnalysis(trimToNull(dto.getAnalysis()));
        question.setStatus(dto.getStatus());
        question.setUpdateTime(now);
        if (creating) {
            question.setCreateTime(now);
        }
    }

    private void replaceOptions(Long questionId,
                                Integer questionType,
                                List<AdminQuestionOptionDTO> options,
                                LocalDateTime now) {
        questionOptionMapper.delete(new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, questionId));
        if (!QuestionTypeEnum.isChoice(questionType) || options == null || options.isEmpty()) {
            return;
        }
        for (AdminQuestionOptionDTO optionDTO : options) {
            if (optionDTO == null) {
                continue;
            }
            QuestionOption option = new QuestionOption();
            option.setQuestionId(questionId);
            option.setOptionLabel(optionDTO.getOptionLabel().trim());
            option.setOptionContent(optionDTO.getOptionContent().trim());
            option.setIsCorrect(optionDTO.getIsCorrect() == null ? 0 : optionDTO.getIsCorrect());
            option.setSortOrder(optionDTO.getSortOrder() == null ? 0 : optionDTO.getSortOrder());
            option.setCreateTime(now);
            questionOptionMapper.insert(option);
        }
    }

    private Map<Long, List<QuestionOption>> loadOptionMap(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QuestionOption> optionList = questionOptionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>()
                        .in(QuestionOption::getQuestionId, questionIds)
                        .orderByAsc(QuestionOption::getSortOrder, QuestionOption::getId)
        );
        return optionList.stream().collect(Collectors.groupingBy(
                QuestionOption::getQuestionId,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    private AdminQuestionVO toQuestionVO(Question question, List<QuestionOption> options) {
        AdminQuestionVO vo = new AdminQuestionVO();
        vo.setId(question.getId());
        vo.setQuestionType(question.getQuestionType());
        vo.setQuestionTypeName(QuestionTypeEnum.getNameByCode(question.getQuestionType()));
        vo.setSceneType(question.getSceneType());
        vo.setSceneTypeName(SceneTypeEnum.getNameByCode(question.getSceneType()));
        vo.setTitle(question.getTitle());
        vo.setContent(question.getContent());
        vo.setAudioUrl(question.getAudioUrl());
        vo.setStandardAnswer(question.getStandardAnswer());
        vo.setScore(question.getScore());
        vo.setDifficulty(question.getDifficulty());
        vo.setDifficultyName(QuestionDifficultyEnum.getNameByCode(question.getDifficulty()));
        vo.setSortOrder(question.getSortOrder());
        vo.setAnalysis(question.getAnalysis());
        vo.setStatus(question.getStatus());
        vo.setCreateTime(question.getCreateTime());
        vo.setUpdateTime(question.getUpdateTime());
        vo.setOptions(toOptionVOList(options));
        return vo;
    }

    private List<AdminQuestionOptionVO> toOptionVOList(List<QuestionOption> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        List<AdminQuestionOptionVO> result = new ArrayList<>();
        for (QuestionOption option : options) {
            AdminQuestionOptionVO vo = new AdminQuestionOptionVO();
            vo.setId(option.getId());
            vo.setOptionLabel(option.getOptionLabel());
            vo.setOptionContent(option.getOptionContent());
            vo.setIsCorrect(option.getIsCorrect());
            vo.setSortOrder(option.getSortOrder());
            result.add(vo);
        }
        return result;
    }

    private void validateQuestionType(Integer questionType) {
        if (questionType == null || QuestionTypeEnum.fromCode(questionType) == null) {
            throw new BusinessException("题型编码不合法");
        }
    }

    private void validateOptionalQuestionType(Integer questionType) {
        if (questionType != null && QuestionTypeEnum.fromCode(questionType) == null) {
            throw new BusinessException("题型编码不合法");
        }
    }

    private void validateSceneType(Integer sceneType) {
        if (sceneType == null || SceneTypeEnum.fromCode(sceneType) == null) {
            throw new BusinessException("场景类型编码不合法");
        }
    }

    private void validateOptionalSceneType(Integer sceneType) {
        if (sceneType != null && SceneTypeEnum.fromCode(sceneType) == null) {
            throw new BusinessException("场景类型编码不合法");
        }
    }

    private void validateDifficulty(Integer difficulty) {
        if (difficulty == null || !QuestionDifficultyEnum.containsCode(difficulty)) {
            throw new BusinessException("难度编码不合法");
        }
    }

    private void validateOptionalDifficulty(Integer difficulty) {
        if (difficulty != null && !QuestionDifficultyEnum.containsCode(difficulty)) {
            throw new BusinessException("难度编码不合法");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (!Objects.equals(status, STATUS_ENABLED) && !Objects.equals(status, STATUS_DISABLED))) {
            throw new BusinessException("题目状态不合法");
        }
    }

    private void validateOptionalStatus(Integer status) {
        if (status != null && !Objects.equals(status, STATUS_ENABLED) && !Objects.equals(status, STATUS_DISABLED)) {
            throw new BusinessException("题目状态不合法");
        }
    }

    private void requireText(String value, String message) {
        if (!hasText(value)) {
            throw new BusinessException(message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }
}