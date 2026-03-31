package com.suncan.english.module.practice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交练习结果参数。
 */
@Data
@Schema(description = "提交练习结果参数")
public class SubmitPracticeDTO {

    @NotNull(message = "taskId cannot be null")
    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @NotNull(message = "startTime cannot be null")
    @Schema(description = "开始时间", example = "2026-03-09T10:00:00")
    private LocalDateTime startTime;

    @Valid
    @NotEmpty(message = "answers cannot be empty")
    @Schema(description = "作答列表")
    private List<PracticeAnswerItemDTO> answers;
}