package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试结果摘要。
 */
@Data
@Schema(description = "测试结果")
public class TestResultVO {

    @Schema(description = "记录ID", example = "10")
    private Long recordId;

    @Schema(description = "试卷ID", example = "1")
    private Long paperId;

    @Schema(description = "总分", example = "80")
    private Integer totalScore;

    @Schema(description = "答对题数", example = "16")
    private Integer correctCount;

    @Schema(description = "等级结果", example = "高级")
    private String levelResult;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "作答耗时（秒）", example = "560")
    private Integer durationSeconds;
}