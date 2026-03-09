package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * One record item in paged list.
 */
@Data
@Schema(description = "Record list item")
public class TestRecordItemVO extends BaseRecordSummaryVO {

    @Schema(description = "Paper name")
    private String paperName;
}
