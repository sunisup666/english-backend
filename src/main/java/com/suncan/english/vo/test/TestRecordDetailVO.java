package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Record detail response object.
 */
@Data
@Schema(description = "Record detail")
public class TestRecordDetailVO extends TestRecordItemVO {

    @Schema(description = "Total question count", example = "20")
    private Integer totalCount;

    @Schema(description = "Per-question detail list")
    private List<QuestionAnswerDetailVO> questionAnswerList;
}
