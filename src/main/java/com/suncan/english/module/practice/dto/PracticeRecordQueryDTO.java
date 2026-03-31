package com.suncan.english.module.practice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 练习记录分页查询条件。
 */
@Data
@Schema(description = "练习记录分页查询条件")
public class PracticeRecordQueryDTO {

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "页码", example = "1")
    private Long current;

    @Schema(description = "每页条数", example = "10")
    private Long size;
}