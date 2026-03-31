package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端题目分页查询参数。
 */
@Data
@Schema(description = "管理端题目分页查询参数")
public class AdminQuestionQueryDTO {

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "场景类型编码", example = "3")
    private Integer sceneType;

    @Schema(description = "难度编码", example = "2")
    private Integer difficulty;

    @Schema(description = "题目状态", example = "1")
    private Integer status;

    @Schema(description = "标题关键词", example = "语法")
    private String keyword;

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;
}