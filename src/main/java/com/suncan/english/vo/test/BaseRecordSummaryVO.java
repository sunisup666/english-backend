package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试记录摘要基础字段。
 *
 * 说明：
 * - levelResult 存编码，便于前端做状态判断；
 * - levelResultName 存中文，便于前端直接展示；
 * - 二者同时返回可减少前后端联调成本。
 */
@Data
public class BaseRecordSummaryVO {

    @Schema(description = "记录ID", example = "101")
    private Long recordId;

    @Schema(description = "试卷ID", example = "1")
    private Long paperId;

    @Schema(description = "总分", example = "85")
    private Integer totalScore;

    @Schema(description = "正确题数", example = "17")
    private Integer correctCount;

    @Schema(description = "等级结果编码：1初级 2中级 3高级", example = "3")
    private Integer levelResult;

    @Schema(description = "等级结果中文名称", example = "高级")
    private String levelResultName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "耗时（秒）", example = "620")
    private Integer durationSeconds;
}