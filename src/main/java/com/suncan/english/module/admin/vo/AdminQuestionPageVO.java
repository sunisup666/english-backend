package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理端题目分页结果。
 */
@Data
@Schema(description = "管理端题目分页结果")
public class AdminQuestionPageVO {

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    @Schema(description = "总记录数", example = "25")
    private Long total;

    @Schema(description = "记录列表")
    private List<AdminQuestionVO> records;
}