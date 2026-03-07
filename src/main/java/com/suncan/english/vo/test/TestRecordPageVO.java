package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 测试记录分页结果。
 */
@Data
@Schema(description = "测试记录分页结果")
public class TestRecordPageVO {

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页条数", example = "10")
    private Long size;

    @Schema(description = "总条数", example = "35")
    private Long total;

    @Schema(description = "记录列表")
    private List<TestRecordItemVO> records;
}

