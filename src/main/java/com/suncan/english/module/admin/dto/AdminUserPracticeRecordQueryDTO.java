package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 管理端用户练习记录查询参数。
 */
@Data
@Schema(description = "管理端用户练习记录查询参数")
public class AdminUserPracticeRecordQueryDTO {

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    @Schema(description = "任务类型编码", example = "1")
    private Integer taskType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期", example = "2026-03-01")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期", example = "2026-03-31")
    private LocalDate endDate;
}