package com.suncan.english.module.admin.controller;

import com.suncan.english.module.admin.dto.AdminLoginDTO;
import com.suncan.english.module.admin.service.AdminService;
import com.suncan.english.module.admin.vo.AdminInfoVO;
import com.suncan.english.shared.common.Result;
import com.suncan.english.shared.context.AdminContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员后台认证接口。
 */
@Tag(name = "管理端-认证", description = "管理员登录与后台身份信息接口")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "管理员登录", description = "管理员账号密码校验成功后返回后台 JWT")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody AdminLoginDTO dto) {
        String token = adminService.login(dto);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @Operation(
            summary = "获取管理员信息",
            description = "返回当前登录管理员的基础信息",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/info")
    public Result<AdminInfoVO> info() {
        Long adminId = AdminContext.getAdminId();
        return Result.success(adminService.getAdminInfo(adminId));
    }
}