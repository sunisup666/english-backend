package com.suncan.english.vo.practice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 训练记录分页结果。
 */
@Data
@Schema(description = "训练记录分页结果")
public class PracticeRecordPageVO {

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "分页大小", example = "10")
    private Long size;

    @Schema(description = "总记录数", example = "25")
    private Long total;

    @Schema(description = "记录列表")
    private List<PracticeRecordItemVO> records;
}
