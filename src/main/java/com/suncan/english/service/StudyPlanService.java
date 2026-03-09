package com.suncan.english.service;

import com.suncan.english.dto.plan.GenerateStudyPlanDTO;
import com.suncan.english.dto.plan.UpdateStudyTaskStatusDTO;
import com.suncan.english.vo.plan.StudyPlanVO;
import com.suncan.english.vo.plan.StudyTaskVO;

import java.util.List;

/**
 * 学习计划业务接口。
 */
public interface StudyPlanService {

    StudyPlanVO generateStudyPlan(Long userId, GenerateStudyPlanDTO dto);

    StudyPlanVO getCurrentPlan(Long userId);

    List<StudyTaskVO> listPlanTasks(Long userId, Long planId);

    void updateTaskStatus(Long userId, UpdateStudyTaskStatusDTO dto);
}
