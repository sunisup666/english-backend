package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端试卷新增/修改参数。
 */
@Data
@Schema(description = "管理端试卷新增或修改参数")
public class AdminPaperSaveDTO {

    @Schema(description = "试卷ID，新增时不传，修改时必传", example = "1")
    private Long id;

    @Schema(description = "试卷名称", example = "英语基础测试卷")
    private String paperName;

    @Schema(description = "试卷描述", example = "适用于基础词汇与语法测试")
    private String description;

    @Schema(description = "总分", example = "100")
    private Integer totalScore;

    @Schema(description = "时长（分钟）", example = "30")
    private Integer durationMinutes;

    @Schema(description = "状态：1启用 0禁用", example = "1")
    private Integer status;
}