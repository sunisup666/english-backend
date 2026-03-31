package com.suncan.english.module.progress.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "学习日历")
public class ProgressCalendarVO {

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "月份", example = "3")
    private Integer month;

    @Schema(description = "学习日期")
    private List<LocalDate> studyDates;
}