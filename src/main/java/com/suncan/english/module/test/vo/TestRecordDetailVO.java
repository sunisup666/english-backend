package com.suncan.english.module.test.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 测试记录详情。
 */
@Data
@Schema(description = "测试记录详情")
public class TestRecordDetailVO extends TestRecordItemVO {

    @Schema(description = "题目总数", example = "20")
    private Integer totalCount;

    @Schema(description = "逐题作答详情")
    private List<QuestionAnswerDetailVO> questionAnswerList;
}