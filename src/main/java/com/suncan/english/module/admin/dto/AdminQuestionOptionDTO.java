package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端题目选项参数。
 */
@Data
@Schema(description = "管理端题目选项参数")
public class AdminQuestionOptionDTO {

    @Schema(description = "选项标签", example = "A")
    private String optionLabel;

    @Schema(description = "选项内容", example = "go")
    private String optionContent;

    @Schema(description = "是否正确：1是 0否", example = "0")
    private Integer isCorrect;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
}