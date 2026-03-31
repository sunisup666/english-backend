package com.suncan.english.module.admin.controller;

import com.suncan.english.module.admin.service.AdminDashboardService;
import com.suncan.english.module.admin.vo.AdminDashboardVO;
import com.suncan.english.shared.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员后台首页统计接口。
 */
@Tag(name = "管理端-首页概览", description = "管理员后台首页统计概览接口")
@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @Operation(
            summary = "获取后台首页概览",
            description = "聚合返回管理员后台首页概览统计信息，用于后台首页卡片展示。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> dashboard() {
        return Result.success(adminDashboardService.getDashboard());
    }
}