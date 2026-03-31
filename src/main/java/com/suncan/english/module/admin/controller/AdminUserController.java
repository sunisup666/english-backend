package com.suncan.english.module.admin.controller;

import com.suncan.english.module.admin.dto.AdminUserPageQueryDTO;
import com.suncan.english.module.admin.dto.AdminUserPracticeRecordQueryDTO;
import com.suncan.english.module.admin.service.AdminUserService;
import com.suncan.english.module.admin.vo.AdminUserDetailVO;
import com.suncan.english.module.admin.vo.AdminUserPageVO;
import com.suncan.english.module.practice.vo.PracticeRecordPageVO;
import com.suncan.english.module.test.dto.TestRecordQueryDTO;
import com.suncan.english.module.test.vo.TestRecordPageVO;
import com.suncan.english.shared.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户管理接口。
 *
 * 职责说明：
 * 1. 提供用户分页列表查询；
 * 2. 提供用户详情与学习画像查看；
 * 3. 提供用户练习记录与测试记录查看。
 */
@Tag(name = "管理端-用户管理", description = "管理员后台用户分页列表、用户详情、用户学习画像、用户练习记录和用户测试记录查看接口")
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(
            summary = "查询用户分页列表",
            description = "分页查询用户基础信息列表，支持按关键词和英语等级筛选；列表侧重账号信息与基础学习概览，不返回完整学习画像。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/page")
    public Result<AdminUserPageVO> page(AdminUserPageQueryDTO queryDTO) {
        return Result.success(adminUserService.page(queryDTO));
    }

    @Operation(
            summary = "查询用户详情",
            description = "查询单个用户的基础信息与学习画像，包括当前学习计划、计划完成率、练习次数、测试次数、最近一次测试结果、积分和徽章数量。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/{id}")
    public Result<AdminUserDetailVO> detail(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        return Result.success(adminUserService.detail(id));
    }

    @Operation(
            summary = "查询用户练习记录",
            description = "分页查询指定用户的练习记录列表，支持按任务类型和日期范围筛选；用于后台查看用户学习过程记录。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/{id}/practice/records")
    public Result<PracticeRecordPageVO> practiceRecords(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            AdminUserPracticeRecordQueryDTO queryDTO
    ) {
        return Result.success(adminUserService.practiceRecords(id, queryDTO));
    }

    @Operation(
            summary = "查询用户测试记录",
            description = "分页查询指定用户的测试记录列表，支持按等级结果和日期范围筛选；字段风格与用户端测试记录保持一致。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/{id}/test/records")
    public Result<TestRecordPageVO> testRecords(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            TestRecordQueryDTO queryDTO
    ) {
        return Result.success(adminUserService.testRecords(id, queryDTO));
    }
}