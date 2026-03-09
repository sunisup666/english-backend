package com.suncan.english.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 生成学习计划请求参数。
 */
@Data
@Schema(description = "生成学习计划参数")
public class GenerateStudyPlanDTO {

    @NotNull(message = "goalType cannot be null")
    @Schema(description = "学习目标：1旅游 2考试 3商务交流", example = "2")
    private Integer goalType;

    @NotNull(message = "dailyMinutes cannot be null")
    @Schema(description = "每日学习时长：30/60/90", example = "60")
    private Integer dailyMinutes;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "计划开始日期（可选，不传默认今天）", example = "2026-03-10")
    private LocalDate startDate;
}
