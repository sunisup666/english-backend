package com.suncan.english.module.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "学习计划分页查询条件")
public class StudyPlanListQueryDTO {

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Long current;

    @Schema(description = "每页条数", example = "10")
    private Long size;
}