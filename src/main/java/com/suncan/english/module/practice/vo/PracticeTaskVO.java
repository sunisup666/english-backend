package com.suncan.english.module.practice.vo;

import com.suncan.english.module.test.vo.QuestionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 任务练习页返回对象。
 */
@Data
@Schema(description = "任务练习内容")
public class PracticeTaskVO {

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "任务类型编码：1词汇 2语法 3听力 4口语", example = "1")
    private Integer taskType;

    @Schema(description = "任务类型名称", example = "词汇")
    private String taskTypeName;

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "题型名称", example = "词汇单选")
    private String questionTypeName;

    @Schema(description = "场景编码", example = "1")
    private Integer sceneType;

    @Schema(description = "场景名称", example = "通用")
    private String sceneTypeName;

    @Schema(description = "任务标题")
    private String taskTitle;

    @Schema(description = "任务内容")
    private String taskContent;

    @Schema(description = "建议时长（分钟）", example = "30")
    private Integer durationMinutes;

    @Schema(description = "任务状态", example = "0")
    private Integer status;

    @Schema(description = "本次实际返回题量", example = "5")
    private Integer totalCount;

    @Schema(description = "题目列表")
    private List<QuestionVO> questionList;
}