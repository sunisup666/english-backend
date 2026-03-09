package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测试结果返回对象。
 *
 * 继承 BaseRecordSummaryVO，已包含：
 * - levelResult（编码）
 * - levelResultName（中文名称）
 */
@Data
@Schema(description = "测试结果")
public class TestResultVO extends BaseRecordSummaryVO {
}