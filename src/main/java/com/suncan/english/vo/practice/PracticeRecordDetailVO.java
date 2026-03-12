package com.suncan.english.vo.practice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 单次训练记录详情。
 */
@Data
@Schema(description = "训练记录详情")
public class PracticeRecordDetailVO {

    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "任务类型编码", example = "1")
    private Integer taskType;

    @Schema(description = "任务类型名称", example = "词汇")
    private String taskTypeName;

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "题型名称", example = "词汇单选")
    private String questionTypeName;

    @Schema(description = "场景编码", example = "2")
    private Integer sceneType;

    @Schema(description = "场景名称", example = "旅游")
    private String sceneTypeName;

    @Schema(description = "总题数", example = "5")
    private Integer totalCount;

    @Schema(description = "正确题数", example = "4")
    private Integer correctCount;

    @Schema(description = "总分", example = "80")
    private Integer totalScore;

    @Schema(description = "时长（秒）", example = "600")
    private Integer durationSeconds;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "逐题作答明细")
    private List<PracticeQuestionAnswerDetailVO> questionAnswerList;
}
