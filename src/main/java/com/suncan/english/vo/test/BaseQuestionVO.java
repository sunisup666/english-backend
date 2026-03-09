package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Base fields shared by question response objects.
 */
@Data
public class BaseQuestionVO {

    @Schema(description = "Question ID", example = "1001")
    private Long questionId;

    @Schema(description = "Question type: 1-vocabulary choice, 2-grammar blank, 3-listening choice, 4-speaking subjective", example = "1")
    private Integer questionType;

    @Schema(description = "Scene type", example = "1")
    private Integer sceneType;

    @Schema(description = "Question title")
    private String title;

    @Schema(description = "Question content")
    private String content;

    @Schema(description = "Audio URL")
    private String audioUrl;
}
