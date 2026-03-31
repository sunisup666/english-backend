package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端试卷分页查询参数。
 */
@Data
@Schema(description = "管理端试卷分页查询参数")
public class AdminPaperQueryDTO {

    @Schema(description = "试卷名称关键词", example = "英语")
    private String keyword;

    @Schema(description = "试卷状态：1启用 0禁用", example = "1")
    private Integer status;

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;
}