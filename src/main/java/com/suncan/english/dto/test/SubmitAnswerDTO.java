package com.suncan.english.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交试卷参数。
 */
@Data
@Schema(description = "提交试卷参数")
public class SubmitAnswerDTO {

    @NotNull(message = "paperId cannot be null")
    @Schema(description = "试卷ID", example = "1")
    private Long paperId;

    @NotNull(message = "startTime cannot be null")
    @Schema(description = "开始答题时间", example = "2026-03-07T10:00:00")
    private LocalDateTime startTime;

    @Valid
    @NotEmpty(message = "answers cannot be empty")
    @Schema(description = "作答列表")
    private List<AnswerItemDTO> answers;
}