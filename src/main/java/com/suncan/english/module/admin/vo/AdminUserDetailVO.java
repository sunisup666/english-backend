package com.suncan.english.module.admin.vo;

import com.suncan.english.module.learning.vo.StudyPlanVO;
import com.suncan.english.module.test.vo.TestResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端用户详情。
 *
 * 结构说明：
 * 1. 基础信息：账号资料与英语等级；
 * 2. 学习画像：当前学习计划、计划完成率、练习次数、测试次数、最近一次测试结果、积分与徽章数量。
 */
@Data
@Schema(description = "管理端用户详情与学习画像")
public class AdminUserDetailVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "英语等级编码", example = "2")
    private Integer englishLevel;

    @Schema(description = "英语等级名称", example = "中级")
    private String englishLevelName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "当前学习计划；无进行中计划时返回 null")
    private StudyPlanVO currentPlan;

    @Schema(description = "当前学习计划完成率，范围 0~1", example = "0.43")
    private BigDecimal planCompletionRate;

    @Schema(description = "练习记录总数", example = "18")
    private Long totalPracticeCount;

    @Schema(description = "测试记录总数", example = "3")
    private Long totalTestCount;

    @Schema(description = "最近一次测试结果；无测试记录时返回 null")
    private TestResultVO latestTestResult;

    @Schema(description = "总积分", example = "120")
    private Integer points;

    @Schema(description = "徽章数量", example = "4")
    private Long badgeCount;
}