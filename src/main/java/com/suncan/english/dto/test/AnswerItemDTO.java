package com.suncan.english.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 单题作答参数。
 */
@Data
@Schema(description = "单题作答参数")
public class AnswerItemDTO {

    @NotNull(message = "questionId cannot be null")
    @Schema(description = "题目ID", example = "1001")
    private Long questionId;

    @Schema(description = "客观题答案（如 A/B/C/D）", example = "A")
    private String answer;

    @Schema(description = "主观题文本答案")
    private String answerText;

    @Schema(description = "口语题语音答案地址")
    private String audioAnswerUrl;
}