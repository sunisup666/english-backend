package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Question object for paper question query API.
 */
@Data
@Schema(description = "Question info")
public class QuestionVO extends BaseQuestionVO {

    @Schema(description = "Score", example = "5")
    private Integer score;

    @Schema(description = "Difficulty", example = "easy")
    private String difficulty;

    @Schema(description = "Sort order", example = "1")
    private Integer sortOrder;

    @Schema(description = "Options")
    private List<QuestionOptionVO> options;
}
