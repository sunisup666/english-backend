package com.suncan.english.controller;

import com.suncan.english.common.Result;
import com.suncan.english.context.UserContext;
import com.suncan.english.dto.practice.PracticeRecordQueryDTO;
import com.suncan.english.dto.practice.SubmitPracticeDTO;
import com.suncan.english.service.PracticeService;
import com.suncan.english.vo.practice.PracticeRecordDetailVO;
import com.suncan.english.vo.practice.PracticeRecordPageVO;
import com.suncan.english.vo.practice.PracticeSubmitResultVO;
import com.suncan.english.vo.practice.PracticeTaskVO;
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
 * 学习任务执行与训练记录接口。
 */
@Tag(name = "学习任务练习模块", description = "进入任务执行页、提交练习结果、查询训练记录")
@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    /**
     * 获取任务练习内容。
     */
    @Operation(
            summary = "获取任务练习内容",
            description = "按任务动态取题，返回任务信息与题目列表",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/task/{taskId}")
    public Result<PracticeTaskVO> getTaskPractice(@PathVariable Long taskId) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.getTaskPractice(userId, taskId));
    }

    /**
     * 提交任务练习结果。
     */
    @Operation(
            summary = "提交任务练习结果",
            description = "判分、保存训练记录与作答明细，提交成功后更新任务状态",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/submit")
    public Result<PracticeSubmitResultVO> submitPractice(@Valid @RequestBody SubmitPracticeDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.submitPractice(userId, dto));
    }

    /**
     * 查询训练记录详情。
     */
    @Operation(
            summary = "查询训练记录详情",
            description = "返回单次训练记录详情与逐题作答明细",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/detail/{recordId}")
    public Result<PracticeRecordDetailVO> recordDetail(@PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.recordDetail(userId, recordId));
    }

    /**
     * 查询训练记录列表（分页）。
     */
    @Operation(
            summary = "查询训练记录列表",
            description = "支持按计划/任务筛选，按提交时间倒序分页返回",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/list")
    public Result<PracticeRecordPageVO> recordList(PracticeRecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        return Result.success(practiceService.recordList(userId, queryDTO));
    }
}
