package com.suncan.english.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学习任务展示对象。
 */
@Data
@Schema(description = "学习任务信息")
public class StudyTaskVO {

    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Schema(description = "所属计划ID", example = "1")
    private Long planId;

    @Schema(description = "任务日期", example = "2026-03-10")
    private LocalDate taskDate;

    @Schema(description = "任务标题", example = "词汇训练（考试）")
    private String taskTitle;

    @Schema(description = "任务内容")
    private String taskContent;

    @Schema(description = "任务类型：1词汇 2语法 3听力 4口语 5阅读", example = "1")
    private Integer taskType;

    @Schema(description = "题型映射（可为空）", example = "1")
    private Integer questionType;

    @Schema(description = "场景类型", example = "3")
    private Integer sceneType;

    @Schema(description = "建议学习时长（分钟）", example = "20")
    private Integer durationMinutes;

    @Schema(description = "任务顺序", example = "1")
    private Integer taskOrder;

    @Schema(description = "状态：0未完成 1已完成", example = "0")
    private Integer status;
}
