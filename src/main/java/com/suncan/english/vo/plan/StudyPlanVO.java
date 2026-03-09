package com.suncan.english.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学习计划展示对象。
 */
@Data
@Schema(description = "学习计划信息")
public class StudyPlanVO {

    @Schema(description = "计划ID", example = "1")
    private Long id;

    @Schema(description = "学习目标：1旅游 2考试 3商务交流", example = "2")
    private Integer goalType;

    @Schema(description = "计划生成时英语等级", example = "初级")
    private String currentLevel;

    @Schema(description = "每日学习时长", example = "60")
    private Integer dailyMinutes;

    @Schema(description = "计划名称", example = "考试英语7天计划-初级-60分钟/天")
    private String planName;

    @Schema(description = "开始日期", example = "2026-03-10")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2026-03-16")
    private LocalDate endDate;

    @Schema(description = "状态：1进行中 2已结束", example = "1")
    private Integer status;
}
