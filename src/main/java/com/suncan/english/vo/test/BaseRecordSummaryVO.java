package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Base fields shared by test record summary objects.
 */
@Data
public class BaseRecordSummaryVO {

    @Schema(description = "Record ID", example = "101")
    private Long recordId;

    @Schema(description = "Paper ID", example = "1")
    private Long paperId;

    @Schema(description = "Total score", example = "85")
    private Integer totalScore;

    @Schema(description = "Correct count", example = "17")
    private Integer correctCount;

    @Schema(description = "Level result", example = "Advanced")
    private String levelResult;

    @Schema(description = "Start time")
    private LocalDateTime startTime;

    @Schema(description = "Submit time")
    private LocalDateTime submitTime;

    @Schema(description = "Duration in seconds", example = "620")
    private Integer durationSeconds;
}
