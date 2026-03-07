package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测试结果摘要。
 * 直接复用记录通用字段，无额外字段。
 */
@Data
@Schema(description = "测试结果")
public class TestResultVO extends BaseRecordSummaryVO {
}
