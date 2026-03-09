package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Paged record response object.
 */
@Data
@Schema(description = "Record page")
public class TestRecordPageVO {

    @Schema(description = "Current page", example = "1")
    private Long current;

    @Schema(description = "Page size", example = "10")
    private Long size;

    @Schema(description = "Total count", example = "35")
    private Long total;

    @Schema(description = "Record list")
    private List<TestRecordItemVO> records;
}
