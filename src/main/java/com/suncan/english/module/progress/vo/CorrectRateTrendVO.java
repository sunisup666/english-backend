package com.suncan.english.module.progress.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "正确率趋势")
public class CorrectRateTrendVO {

    @Schema(description = "周标签")
    private List<String> weeks;

    @Schema(description = "词汇正确率")
    private List<BigDecimal> vocabulary;

    @Schema(description = "语法正确率")
    private List<BigDecimal> grammar;

    @Schema(description = "听力正确率")
    private List<BigDecimal> listening;

    @Schema(description = "口语正确率")
    private List<BigDecimal> speaking;
}