package com.suncan.english.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 测试记录分页查询条件。
 */
@Data
@Schema(description = "测试记录分页查询条件")
public class TestRecordQueryDTO {

    @Schema(description = "页码，从 1 开始", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;

    @Schema(description = "试卷ID")
    private Long paperId;

    @Schema(description = "等级结果", example = "中级")
    private String levelResult;

    @Schema(description = "题目类型：1词汇单选 2语法填空 3听力选择 4口语主观", example = "3")
    private Integer questionType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期", example = "2026-03-01")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期", example = "2026-03-31")
    private LocalDate endDate;
}
