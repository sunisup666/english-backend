package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户分页列表项。
 */
@Data
@Schema(description = "管理端用户分页列表项")
public class AdminUserPageItemVO {

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "当前学习计划名称")
    private String currentPlanName;

    @Schema(description = "练习记录总数", example = "18")
    private Long totalPracticeCount;

    @Schema(description = "测试记录总数", example = "3")
    private Long totalTestCount;
}