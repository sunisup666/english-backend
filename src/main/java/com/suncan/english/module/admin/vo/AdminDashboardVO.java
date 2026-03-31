package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员后台首页统计概览。
 */
@Data
@Schema(description = "管理员后台首页统计概览")
public class AdminDashboardVO {

    @Schema(description = "用户总数", example = "128")
    private Long userCount;

    @Schema(description = "题目总数", example = "560")
    private Long questionCount;

    @Schema(description = "试卷总数", example = "12")
    private Long paperCount;

    @Schema(description = "练习记录总数", example = "3460")
    private Long practiceRecordCount;

    @Schema(description = "测试记录总数", example = "820")
    private Long testRecordCount;

    @Schema(description = "进行中学习计划数", example = "37")
    private Long activePlanCount;

    @Schema(description = "今日新增用户数", example = "5")
    private Long todayNewUserCount;
}