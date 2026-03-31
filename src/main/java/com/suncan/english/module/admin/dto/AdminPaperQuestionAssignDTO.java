package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 试卷题目编排项参数。
 */
@Data
@Schema(description = "试卷题目编排项参数")
public class AdminPaperQuestionAssignDTO {

    @Schema(description = "题目ID", example = "101")
    private Long questionId;

    @Schema(description = "该题在试卷中的分值", example = "5")
    private Integer score;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
}