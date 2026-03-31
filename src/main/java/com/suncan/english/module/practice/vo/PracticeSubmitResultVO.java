package com.suncan.english.module.practice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 提交学习任务练习后的结果摘要。
 */
@Data
@Schema(description = "学习任务练习提交结果")
public class PracticeSubmitResultVO {

    @Schema(description = "练习记录ID", example = "1")
    private Long recordId;

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "总题数", example = "5")
    private Integer totalCount;

    @Schema(description = "正确题数", example = "4")
    private Integer correctCount;

    @Schema(description = "总分", example = "80")
    private Integer totalScore;

    @Schema(description = "作答时长（秒）", example = "600")
    private Integer durationSeconds;
}