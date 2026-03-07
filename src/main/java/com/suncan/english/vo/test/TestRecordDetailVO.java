package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 测试记录详情。
 * 继承记录摘要字段，再追加总题数和逐题作答明细。
 */
@Data
@Schema(description = "测试记录详情")
public class TestRecordDetailVO extends TestRecordItemVO {

    @Schema(description = "总题数", example = "20")
    private Integer totalCount;

    @Schema(description = "逐题作答明细")
    private List<QuestionAnswerDetailVO> questionAnswerList;
}
