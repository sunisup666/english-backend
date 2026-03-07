package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页列表中的单条测试记录摘要。
 * 在通用记录字段基础上增加试卷名称。
 */
@Data
@Schema(description = "测试记录摘要")
public class TestRecordItemVO extends BaseRecordSummaryVO {

    @Schema(description = "试卷名称")
    private String paperName;
}
