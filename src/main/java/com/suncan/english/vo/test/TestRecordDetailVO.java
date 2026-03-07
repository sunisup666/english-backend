package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试记录详情。
 */
@Data
@Schema(description = "测试记录详情")
public class TestRecordDetailVO {

    @Schema(description = "记录ID", example = "101")
    private Long recordId;

    @Schema(description = "试卷ID", example = "1")
    private Long paperId;

    @Schema(description = "试卷名称")
    private String paperName;

    @Schema(description = "总分", example = "88")
    private Integer totalScore;

    @Schema(description = "答对题数", example = "18")
    private Integer correctCount;

    @Schema(description = "总题数", example = "20")
    private Integer totalCount;

    @Schema(description = "等级结果", example = "高级")
    private String levelResult;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "作答耗时（秒）", example = "620")
    private Integer durationSeconds;

    @Schema(description = "逐题作答明细")
    private List<QuestionAnswerDetailVO> questionAnswerList;
}

