package com.suncan.english.module.user.controller;

import com.suncan.english.module.user.dto.LoginDTO;
import com.suncan.english.module.user.dto.RegisterDTO;
import com.suncan.english.module.user.dto.UpdatePasswordDTO;
import com.suncan.english.module.user.dto.UpdateUserDTO;
import com.suncan.english.module.user.service.UserService;
import com.suncan.english.module.user.vo.UserDashboardVO;
import com.suncan.english.module.user.vo.UserInfoVO;
import com.suncan.english.shared.common.Result;
import com.suncan.english.shared.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块接口。
 */
@Tag(name = "用户端-用户模块", description = "用户注册登录、个人信息与首页概览接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册", description = "创建普通用户账号，默认英语等级为初级")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "用户登录", description = "用户名密码校验成功后返回 token")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @Operation(
            summary = "获取个人信息",
            description = "返回当前登录用户的基础信息以及英语等级名称",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        Long userId = UserContext.getUserId();
        return Result.success(userService.getUserInfo(userId));
    }

    @Operation(
            summary = "获取首页概览",
            description = "聚合返回当前登录用户的首页概览信息，包括用户基本信息、当前学习计划进度与最近一次测试结果。部分扩展统计字段当前阶段先返回默认值。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/dashboard")
    public Result<UserDashboardVO> dashboard() {
        Long userId = UserContext.getUserId();
        return Result.success(userService.getDashboard(userId));
    }

    @Operation(
            summary = "修改个人信息",
            description = "可修改昵称、邮箱、手机号，至少传一个字段",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody UpdateUserDTO dto) {
        Long userId = UserContext.getUserId();
        userService.updateUser(userId, dto);
        return Result.success();
    }

    @Operation(
            summary = "修改密码",
            description = "先校验旧密码，再更新新密码",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        userService.updatePassword(userId, dto);
        return Result.success();
    }
}