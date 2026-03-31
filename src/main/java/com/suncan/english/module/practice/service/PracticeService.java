package com.suncan.english.module.practice.service;

import com.suncan.english.module.practice.dto.PracticeRecordQueryDTO;
import com.suncan.english.module.practice.dto.SubmitPracticeDTO;
import com.suncan.english.module.practice.vo.PracticeRecordDetailVO;
import com.suncan.english.module.practice.vo.PracticeRecordPageVO;
import com.suncan.english.module.practice.vo.PracticeSubmitResultVO;
import com.suncan.english.module.practice.vo.PracticeTaskVO;

/**
 * 学习任务执行与训练记录业务接口。
 */
public interface PracticeService {

    PracticeTaskVO getTaskPractice(Long userId, Long taskId);

    PracticeSubmitResultVO submitPractice(Long userId, SubmitPracticeDTO dto);

    PracticeRecordDetailVO recordDetail(Long userId, Long recordId);

    PracticeRecordPageVO recordList(Long userId, PracticeRecordQueryDTO queryDTO);
}