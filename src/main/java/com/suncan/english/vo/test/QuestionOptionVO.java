package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Question option response object.
 */
@Data
@Schema(description = "Question option")
public class QuestionOptionVO {

    @Schema(description = "Option label", example = "A")
    private String optionLabel;

    @Schema(description = "Option content", example = "I go to school by bus.")
    private String optionContent;

    @Schema(description = "Sort order", example = "1")
    private Integer sortOrder;
}
