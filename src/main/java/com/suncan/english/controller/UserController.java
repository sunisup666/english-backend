package com.suncan.english.controller;

import com.suncan.english.common.Result;
import com.suncan.english.dto.LoginDTO;
import com.suncan.english.dto.RegisterDTO;
import com.suncan.english.dto.UpdatePasswordDTO;
import com.suncan.english.dto.UpdateUserDTO;
import com.suncan.english.entity.User;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.interceptor.LoginInterceptor;
import com.suncan.english.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
 * 用户模块接口，主要包含注册、登录和个人信息维护。
 */
@Tag(name = "用户模块", description = "用户注册登录与个人信息管理")
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

    @Operation(summary = "获取个人信息", description = "需要在请求头携带 Authorization: Bearer {token}")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(userService.getUserInfo(userId));
    }

    @Operation(summary = "修改个人信息", description = "可修改昵称、邮箱、手机号，至少传一个字段")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody UpdateUserDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        userService.updateUser(userId, dto);
        return Result.success();
    }

    @Operation(summary = "修改密码", description = "先校验旧密码，再更新新密码")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        userService.updatePassword(userId, dto);
        return Result.success();
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute(LoginInterceptor.CURRENT_USER_ID);
        if (userIdObj == null) {
            throw new BusinessException("请先登录");
        }
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        return Long.parseLong(userIdObj.toString());
    }
}
