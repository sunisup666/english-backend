package com.suncan.english.module.practice.controller;

import com.suncan.english.module.practice.dto.PracticeRecordQueryDTO;
import com.suncan.english.module.practice.dto.SubmitPracticeDTO;
import com.suncan.english.module.practice.service.PracticeService;
import com.suncan.english.module.practice.vo.PracticeRecordDetailVO;
import com.suncan.english.module.practice.vo.PracticeRecordPageVO;
import com.suncan.english.module.practice.vo.PracticeSubmitResultVO;
import com.suncan.english.module.practice.vo.PracticeTaskVO;
import com.suncan.english.shared.common.Result;
import com.suncan.english.shared.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习任务练习模块接口。
 */
@Tag(name = "用户端-学习任务练习模块", description = "进入任务执行页、提交练习结果、查询训练记录")
@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @Operation(
            summary = "获取任务练习内容",
            description = "根据任务 ID 获取练习页所需的题目内容",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/task/{taskId}")
    public Result<PracticeTaskVO> getTaskPractice(@PathVariable Long taskId) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.getTaskPractice(userId, taskId));
    }

    @Operation(
            summary = "提交练习结果",
            description = "提交当前任务的练习答案并返回结果摘要",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/submit")
    public Result<PracticeSubmitResultVO> submitPractice(@Valid @RequestBody SubmitPracticeDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.submitPractice(userId, dto));
    }

    @Operation(
            summary = "查询练习记录详情",
            description = "根据练习记录 ID 查询练习详情",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/detail/{recordId}")
    public Result<PracticeRecordDetailVO> recordDetail(@PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.recordDetail(userId, recordId));
    }

    @Operation(
            summary = "查询练习记录列表",
            description = "分页查询当前登录用户的练习记录列表",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/list")
    public Result<PracticeRecordPageVO> recordList(PracticeRecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.recordList(userId, queryDTO));
    }
}