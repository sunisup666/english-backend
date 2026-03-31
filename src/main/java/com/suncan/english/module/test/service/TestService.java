package com.suncan.english.module.test.service;

import com.suncan.english.module.test.dto.SubmitAnswerDTO;
import com.suncan.english.module.test.dto.TestRecordQueryDTO;
import com.suncan.english.module.test.vo.QuestionVO;
import com.suncan.english.module.test.vo.TestRecordDetailVO;
import com.suncan.english.module.test.vo.TestRecordPageVO;
import com.suncan.english.module.test.vo.TestResultVO;

import java.util.List;

/**
 * 语言能力评估业务接口。
 */
public interface TestService {

    List<QuestionVO> getQuestions(Long paperId);

    TestResultVO submitAnswers(Long userId, SubmitAnswerDTO dto);

    TestResultVO latestResult(Long userId);

    TestRecordPageVO queryRecordPage(Long userId, TestRecordQueryDTO queryDTO);

    TestRecordDetailVO recordDetail(Long userId, Long recordId);
}