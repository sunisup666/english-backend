package com.suncan.english.module.user.vo;

import com.suncan.english.module.test.vo.TestResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "用户首页概览")
public class UserDashboardVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "英语等级编码", example = "2")
    private Integer englishLevel;

    @Schema(description = "英语等级名称", example = "中级")
    private String englishLevelName;

    @Schema(description = "今日任务总数", example = "3")
    private Long todayTaskTotal;

    @Schema(description = "今日已完成任务数", example = "1")
    private Long todayCompletedTaskCount;

    @Schema(description = "累计练习次数", example = "18")
    private Long totalPracticeCount;

    @Schema(description = "累计学习分钟数", example = "240")
    private Long totalStudyMinutes;

    @Schema(description = "总正确率", example = "0.78")
    private BigDecimal totalCorrectRate;

    @Schema(description = "各题型练习次数")
    private Map<String, Long> practiceByType;

    @Schema(description = "各题型正确率")
    private Map<String, BigDecimal> correctRateByType;

    @Schema(description = "连续学习天数", example = "3")
    private Integer continuousStudyDays;

    @Schema(description = "当前计划任务总数", example = "28")
    private Long planTotalTask;

    @Schema(description = "当前计划已完成任务数", example = "12")
    private Long planCompletedTask;

    @Schema(description = "当前计划完成率", example = "0.43")
    private BigDecimal planCompletionRate;

    @Schema(description = "测试次数", example = "3")
    private Long totalTestCount;

    @Schema(description = "等级趋势")
    private List<LevelTrendItem> levelTrend;

    @Schema(description = "最近一次测试结果")
    private TestResultVO latestTestResult;

    @Data
    @Schema(description = "等级趋势项")
    public static class LevelTrendItem {

        @Schema(description = "提交日期", example = "2026-03-10")
        private LocalDate submitTime;

        @Schema(description = "等级结果", example = "2")
        private Integer levelResult;

        @Schema(description = "等级名称", example = "中级")
        private String levelResultName;
    }
}
