package com.suncan.english.module.test.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测试记录分页列表单项。
 */
@Data
@Schema(description = "测试记录列表项")
public class TestRecordItemVO extends BaseRecordSummaryVO {

    @Schema(description = "试卷名称")
    private String paperName;
}