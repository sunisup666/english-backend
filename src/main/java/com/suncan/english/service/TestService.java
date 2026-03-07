package com.suncan.english.service;

import com.suncan.english.dto.test.SubmitAnswerDTO;
import com.suncan.english.dto.test.TestRecordQueryDTO;
import com.suncan.english.vo.test.QuestionVO;
import com.suncan.english.vo.test.TestRecordDetailVO;
import com.suncan.english.vo.test.TestRecordPageVO;
import com.suncan.english.vo.test.TestResultVO;

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