package com.suncan.english.module.progress.controller;

import com.suncan.english.module.progress.service.ProgressService;
import com.suncan.english.module.progress.vo.CorrectRateTrendVO;
import com.suncan.english.module.progress.vo.ProgressCalendarVO;
import com.suncan.english.shared.common.Result;
import com.suncan.english.shared.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端-进度模块", description = "学习进度与正确率趋势")
@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @Operation(summary = "学习日历", description = "查询用户学习日历", security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/calendar")
    public Result<ProgressCalendarVO> calendar(
            @Parameter(description = "年份") @RequestParam(required = false) Integer year,
            @Parameter(description = "月份") @RequestParam(required = false) Integer month) {
        Long userId = UserContext.getUserId();
        return Result.success(progressService.getCalendar(userId, year, month));
    }

    @Operation(summary = "正确率趋势", description = "查询最近几周正确率趋势", security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/correct-rate")
    public Result<CorrectRateTrendVO> correctRate() {
        Long userId = UserContext.getUserId();
        return Result.success(progressService.getCorrectRateTrend(userId));
    }
}