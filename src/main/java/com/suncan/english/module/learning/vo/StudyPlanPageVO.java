package com.suncan.english.module.learning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "学习计划分页结果")
public class StudyPlanPageVO {

    @Schema(description = "页码", example = "1")
    private Long current;

    @Schema(description = "每页条数", example = "10")
    private Long size;

    @Schema(description = "总记录数", example = "25")
    private Long total;

    @Schema(description = "记录列表")
    private List<StudyPlanVO> records;
}