package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Test result summary response.
 */
@Data
@Schema(description = "Test result")
public class TestResultVO extends BaseRecordSummaryVO {
}
