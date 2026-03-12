package com.suncan.english.service;

import com.suncan.english.dto.practice.PracticeRecordQueryDTO;
import com.suncan.english.dto.practice.SubmitPracticeDTO;
import com.suncan.english.vo.practice.PracticeRecordDetailVO;
import com.suncan.english.vo.practice.PracticeRecordPageVO;
import com.suncan.english.vo.practice.PracticeSubmitResultVO;
import com.suncan.english.vo.practice.PracticeTaskVO;

/**
 * 学习任务执行与训练记录业务接口。
 */
public interface PracticeService {

    PracticeTaskVO getTaskPractice(Long userId, Long taskId);

    PracticeSubmitResultVO submitPractice(Long userId, SubmitPracticeDTO dto);

    PracticeRecordDetailVO recordDetail(Long userId, Long recordId);

    PracticeRecordPageVO recordList(Long userId, PracticeRecordQueryDTO queryDTO);
}
