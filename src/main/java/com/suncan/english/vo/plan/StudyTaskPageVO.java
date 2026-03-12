package com.suncan.english.vo.plan;

import lombok.Data;

import java.util.List;

/**
 * 学习任务分页返回对象。
 *
 * 说明：
 * 1. 该 VO 只承载分页元信息和任务列表数据；
 * 2. records 沿用现有 StudyTaskVO，保持前端任务项字段兼容；
 * 3. 通过统一分页结构，后续历史任务、筛选视图都可复用同一接口返回格式。
 */
@Data
public class StudyTaskPageVO {

    /**
     * 当前页码。
     */
    private Long current;

    /**
     * 每页条数。
     */
    private Long size;

    /**
     * 总记录数。
     */
    private Long total;

    /**
     * 当前页任务列表。
     */
    private List<StudyTaskVO> records;
}
