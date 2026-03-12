package com.suncan.english.service;

import com.suncan.english.dto.plan.GenerateStudyPlanDTO;
import com.suncan.english.dto.plan.StudyTaskQueryDTO;
import com.suncan.english.dto.plan.UpdateStudyTaskStatusDTO;
import com.suncan.english.vo.plan.StudyPlanVO;
import com.suncan.english.vo.plan.StudyTaskPageVO;
import com.suncan.english.vo.plan.StudyTaskVO;

/**
 * 学习计划业务接口。
 */
public interface StudyPlanService {

    /**
     * 生成学习计划。
     */
    StudyPlanVO generateStudyPlan(Long userId, GenerateStudyPlanDTO dto);

    /**
     * 查询当前进行中的学习计划。
     */
    StudyPlanVO getCurrentPlan(Long userId);

    /**
     * 分页查询学习任务列表。
     *
     * 说明：
     * 1. 使用查询 DTO 统一承载 planId、日期、状态、类型、分页参数，避免方法参数不断膨胀；
     * 2. 返回分页结构而非纯列表，更适合“历史任务”与“筛选列表”的长期扩展；
     * 3. 统一接口后，前端通过是否传 taskDate 就能实现“当天/全部”两种视图，不需要新开子接口。
     */
    StudyTaskPageVO listPlanTasks(Long userId, StudyTaskQueryDTO queryDTO);

    /**
     * 查询单个学习任务详情。
     */
    StudyTaskVO taskDetail(Long userId, Long taskId);

    /**
     * 更新学习任务完成状态。
     */
    void updateTaskStatus(Long userId, UpdateStudyTaskStatusDTO dto);
}
