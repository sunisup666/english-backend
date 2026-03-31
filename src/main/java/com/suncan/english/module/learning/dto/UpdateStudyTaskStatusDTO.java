package com.suncan.english.module.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新学习任务状态请求参数。
 */
@Data
@Schema(description = "更新学习任务状态参数")
public class UpdateStudyTaskStatusDTO {

    @NotNull(message = "taskId cannot be null")
    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @NotNull(message = "status cannot be null")
    @Schema(description = "任务状态：0未完成 1已完成", example = "1")
    private Integer status;
}