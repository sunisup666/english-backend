package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端试卷题目关联信息。
 */
@Data
@Schema(description = "管理端试卷题目关联信息")
public class AdminPaperQuestionVO {

    @Schema(description = "题目ID", example = "101")
    private Long questionId;

    @Schema(description = "试卷分值", example = "5")
    private Integer score;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "题型名称", example = "词汇单选")
    private String questionTypeName;

    @Schema(description = "场景类型编码", example = "3")
    private Integer sceneType;

    @Schema(description = "场景类型名称", example = "考试")
    private String sceneTypeName;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题干内容")
    private String content;

    @Schema(description = "题目状态：1启用 0禁用", example = "1")
    private Integer status;
}