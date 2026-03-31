package com.suncan.english.module.learning.service;

import com.suncan.english.module.learning.dto.GenerateStudyPlanDTO;
import com.suncan.english.module.learning.dto.StudyPlanListQueryDTO;
import com.suncan.english.module.learning.dto.StudyTaskQueryDTO;
import com.suncan.english.module.learning.dto.UpdateStudyTaskStatusDTO;
import com.suncan.english.module.learning.vo.StudyPlanPageVO;
import com.suncan.english.module.learning.vo.StudyPlanVO;
import com.suncan.english.module.learning.vo.StudyTaskPageVO;
import com.suncan.english.module.learning.vo.StudyTaskVO;

public interface StudyPlanService {

    StudyPlanVO generateStudyPlan(Long userId, GenerateStudyPlanDTO dto);

    StudyPlanVO getCurrentPlan(Long userId);

    StudyPlanPageVO listPlans(Long userId, StudyPlanListQueryDTO queryDTO);

    StudyTaskPageVO listPlanTasks(Long userId, StudyTaskQueryDTO queryDTO);

    StudyTaskVO taskDetail(Long userId, Long taskId);

    void updateTaskStatus(Long userId, UpdateStudyTaskStatusDTO dto);
}