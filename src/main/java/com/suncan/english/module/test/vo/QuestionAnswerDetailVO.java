package com.suncan.english.module.test.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Per-question answer detail in record detail API.
 */
@Data
@Schema(description = "Per-question answer detail")
public class QuestionAnswerDetailVO extends BaseQuestionVO {

    @Schema(description = "User choice/text answer", example = "A")
    private String userAnswer;

    @Schema(description = "User text answer for subjective question")
    private String answerText;

    @Schema(description = "User audio answer URL")
    private String audioAnswerUrl;

    @Schema(description = "Is correct: 1-yes, 0-no", example = "1")
    private Integer isCorrect;

    @Schema(description = "Score for this question", example = "5")
    private Integer score;

    @Schema(description = "Standard answer")
    private String standardAnswer;

    @Schema(description = "Analysis")
    private String analysis;

    @Schema(description = "Options")
    private List<QuestionOptionVO> optionList;
}