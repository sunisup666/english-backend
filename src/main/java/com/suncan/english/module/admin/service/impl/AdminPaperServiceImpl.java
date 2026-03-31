package com.suncan.english.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suncan.english.module.admin.dto.AdminPaperQueryDTO;
import com.suncan.english.module.admin.dto.AdminPaperQuestionAssignDTO;
import com.suncan.english.module.admin.dto.AdminPaperQuestionAssignRequestDTO;
import com.suncan.english.module.admin.dto.AdminPaperSaveDTO;
import com.suncan.english.module.admin.service.AdminPaperService;
import com.suncan.english.module.admin.vo.AdminPaperPageVO;
import com.suncan.english.module.admin.vo.AdminPaperQuestionVO;
import com.suncan.english.module.admin.vo.AdminPaperVO;
import com.suncan.english.module.questionbank.entity.PaperQuestion;
import com.suncan.english.module.questionbank.entity.Question;
import com.suncan.english.module.questionbank.entity.TestPaper;
import com.suncan.english.module.questionbank.mapper.PaperQuestionMapper;
import com.suncan.english.module.questionbank.mapper.QuestionMapper;
import com.suncan.english.module.questionbank.mapper.TestPaperMapper;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminPaperServiceImpl implements AdminPaperService {

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;

    private final TestPaperMapper testPaperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;

    public AdminPaperServiceImpl(TestPaperMapper testPaperMapper,
                                 PaperQuestionMapper paperQuestionMapper,
                                 QuestionMapper questionMapper) {
        this.testPaperMapper = testPaperMapper;
        this.paperQuestionMapper = paperQuestionMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    public AdminPaperPageVO page(AdminPaperQueryDTO queryDTO) {
        long current = queryDTO == null || queryDTO.getCurrent() == null || queryDTO.getCurrent() < 1 ? 1 : queryDTO.getCurrent();
        long size = queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() < 1 ? 10 : Math.min(queryDTO.getSize(), 50);
        validateQuery(queryDTO);

        Long total = testPaperMapper.selectCount(buildPageWrapper(queryDTO));
        if (total == null || total == 0L) {
            AdminPaperPageVO empty = new AdminPaperPageVO();
            empty.setCurrent(current);
            empty.setSize(size);
            empty.setTotal(0L);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        long offset = (current - 1) * size;
        List<TestPaper> paperList = testPaperMapper.selectList(
                buildPageWrapper(queryDTO)
                        .orderByDesc(TestPaper::getUpdateTime)
                        .orderByDesc(TestPaper::getId)
                        .last("limit " + offset + "," + size)
        );

        AdminPaperPageVO pageVO = new AdminPaperPageVO();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(paperList.stream().map(this::toPaperVO).collect(Collectors.toList()));
        return pageVO;
    }

    @Override
    public AdminPaperVO detail(Long id) {
        if (id == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        TestPaper paper = requirePaper(id);
        return toPaperVO(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AdminPaperSaveDTO dto) {
        validateSaveDTO(dto);
        LocalDateTime now = LocalDateTime.now();

        TestPaper paper = new TestPaper();
        fillPaper(paper, dto, now, true);
        testPaperMapper.insert(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AdminPaperSaveDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException("修改试卷时试卷ID不能为空");
        }
        TestPaper paper = requirePaper(dto.getId());
        validateSaveDTO(dto);

        fillPaper(paper, dto, LocalDateTime.now(), false);
        testPaperMapper.updateById(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        requirePaper(id);
        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id));
        testPaperMapper.deleteById(id);
    }

    @Override
    public List<AdminPaperQuestionVO> listQuestions(Long paperId) {
        requirePaper(paperId);
        List<PaperQuestion> relationList = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId)
                        .orderByAsc(PaperQuestion::getSortOrder, PaperQuestion::getId)
        );
        if (relationList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = relationList.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questionList = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getId, questionIds)
        );
        Map<Long, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, item -> item));

        List<AdminPaperQuestionVO> result = new ArrayList<>();
        for (PaperQuestion relation : relationList) {
            Question question = questionMap.get(relation.getQuestionId());
            AdminPaperQuestionVO vo = new AdminPaperQuestionVO();
            vo.setQuestionId(relation.getQuestionId());
            vo.setScore(relation.getScore());
            vo.setSortOrder(relation.getSortOrder());
            if (question != null) {
                vo.setQuestionType(question.getQuestionType());
                vo.setQuestionTypeName(QuestionTypeEnum.getNameByCode(question.getQuestionType()));
                vo.setSceneType(question.getSceneType());
                vo.setSceneTypeName(SceneTypeEnum.getNameByCode(question.getSceneType()));
                vo.setTitle(question.getTitle());
                vo.setContent(question.getContent());
                vo.setStatus(question.getStatus());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestions(Long paperId, AdminPaperQuestionAssignRequestDTO dto) {
        TestPaper paper = requirePaper(paperId);
        List<AdminPaperQuestionAssignDTO> questions = dto == null ? null : dto.getQuestions();
        validateAssignDTO(questions);

        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId));
        if (questions == null || questions.isEmpty()) {
            paper.setTotalScore(0);
            paper.setUpdateTime(LocalDateTime.now());
            testPaperMapper.updateById(paper);
            return;
        }

        List<Long> questionIds = questions.stream().map(AdminPaperQuestionAssignDTO::getQuestionId).collect(Collectors.toList());
        List<Question> existedQuestions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .in(Question::getId, questionIds)
                        .select(Question::getId)
        );
        Set<Long> existedQuestionIds = existedQuestions.stream().map(Question::getId).collect(Collectors.toSet());
        for (Long questionId : questionIds) {
            if (!existedQuestionIds.contains(questionId)) {
                throw new BusinessException("存在无效题目ID：" + questionId);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        int totalScore = 0;
        for (AdminPaperQuestionAssignDTO item : questions) {
            PaperQuestion relation = new PaperQuestion();
            relation.setPaperId(paperId);
            relation.setQuestionId(item.getQuestionId());
            relation.setScore(item.getScore());
            relation.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
            relation.setCreateTime(now);
            paperQuestionMapper.insert(relation);
            totalScore += item.getScore() == null ? 0 : item.getScore();
        }

        paper.setTotalScore(totalScore);
        paper.setUpdateTime(now);
        testPaperMapper.updateById(paper);
    }

    private void validateQuery(AdminPaperQueryDTO queryDTO) {
        if (queryDTO == null) {
            return;
        }
        if (queryDTO.getStatus() != null) {
            validateStatus(queryDTO.getStatus());
        }
    }

    private LambdaQueryWrapper<TestPaper> buildPageWrapper(AdminPaperQueryDTO queryDTO) {
        LambdaQueryWrapper<TestPaper> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO == null) {
            return wrapper;
        }
        String keyword = trimToNull(queryDTO.getKeyword());
        wrapper.eq(queryDTO.getStatus() != null, TestPaper::getStatus, queryDTO.getStatus())
                .like(keyword != null, TestPaper::getPaperName, keyword);
        return wrapper;
    }

    private void validateSaveDTO(AdminPaperSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException("试卷参数不能为空");
        }
        requireText(dto.getPaperName(), "试卷名称不能为空");
        if (dto.getDurationMinutes() == null || dto.getDurationMinutes() <= 0) {
            throw new BusinessException("试卷时长必须大于0");
        }
        if (dto.getTotalScore() == null || dto.getTotalScore() < 0) {
            throw new BusinessException("试卷总分不能为空且不能小于0");
        }
        validateStatus(dto.getStatus());
    }

    private void validateAssignDTO(List<AdminPaperQuestionAssignDTO> questions) {
        if (questions == null) {
            return;
        }
        Set<Long> uniqueQuestionIds = questions.stream()
                .map(AdminPaperQuestionAssignDTO::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uniqueQuestionIds.size() != questions.size()) {
            throw new BusinessException("试卷题目不能重复");
        }
        for (AdminPaperQuestionAssignDTO item : questions) {
            if (item == null || item.getQuestionId() == null) {
                throw new BusinessException("题目ID不能为空");
            }
            if (item.getScore() == null || item.getScore() < 0) {
                throw new BusinessException("题目分值不能为空且不能小于0");
            }
        }
    }

    private TestPaper requirePaper(Long id) {
        if (id == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        TestPaper paper = testPaperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        return paper;
    }

    private void fillPaper(TestPaper paper, AdminPaperSaveDTO dto, LocalDateTime now, boolean creating) {
        paper.setPaperName(dto.getPaperName().trim());
        paper.setDescription(trimToNull(dto.getDescription()));
        paper.setTotalScore(dto.getTotalScore());
        paper.setDurationMinutes(dto.getDurationMinutes());
        paper.setStatus(dto.getStatus());
        paper.setUpdateTime(now);
        if (creating) {
            paper.setCreateTime(now);
        }
    }

    private AdminPaperVO toPaperVO(TestPaper paper) {
        AdminPaperVO vo = new AdminPaperVO();
        vo.setId(paper.getId());
        vo.setPaperName(paper.getPaperName());
        vo.setDescription(paper.getDescription());
        vo.setTotalScore(paper.getTotalScore());
        vo.setDurationMinutes(paper.getDurationMinutes());
        vo.setStatus(paper.getStatus());
        vo.setCreateTime(paper.getCreateTime());
        vo.setUpdateTime(paper.getUpdateTime());
        return vo;
    }

    private void validateStatus(Integer status) {
        if (!Objects.equals(status, STATUS_ENABLED) && !Objects.equals(status, STATUS_DISABLED)) {
            throw new BusinessException("试卷状态不合法");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}