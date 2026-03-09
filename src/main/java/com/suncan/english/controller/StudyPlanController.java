package com.suncan.english.controller;

import com.suncan.english.common.Result;
import com.suncan.english.context.UserContext;
import com.suncan.english.dto.plan.GenerateStudyPlanDTO;
import com.suncan.english.dto.plan.UpdateStudyTaskStatusDTO;
import com.suncan.english.service.StudyPlanService;
import com.suncan.english.vo.plan.StudyPlanVO;
import com.suncan.english.vo.plan.StudyTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学习计划模块接口。
 */
@Validated
@Tag(name = "学习计划模块", description = "个性化学习计划与任务管理接口")
@RestController
@RequestMapping("/api/plan")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @Operation(
            summary = "生成学习计划",
            description = "根据学习目标、当前等级、每日学习时长生成7天计划和任务",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/generate")
    public Result<StudyPlanVO> generate(@Valid @RequestBody GenerateStudyPlanDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.generateStudyPlan(userId, dto));
    }

    @Operation(
            summary = "查询当前学习计划",
            description = "查询当前登录用户状态为进行中的计划",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/current")
    public Result<StudyPlanVO> current() {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.getCurrentPlan(userId));
    }

    @Operation(
            summary = "查询计划任务列表",
            description = "按计划ID返回任务列表，包含完成状态",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/task/list")
    public Result<List<StudyTaskVO>> taskList(
            @Parameter(description = "计划ID", required = true)
            @RequestParam @NotNull(message = "planId cannot be null") Long planId) {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.listPlanTasks(userId, planId));
    }

    @Operation(
            summary = "更新任务完成状态",
            description = "将任务标记为已完成或未完成",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PutMapping("/task/status")
    public Result<Void> updateTaskStatus(@Valid @RequestBody UpdateStudyTaskStatusDTO dto) {
        Long userId = UserContext.getUserId();
        studyPlanService.updateTaskStatus(userId, dto);
        return Result.success();
    }
}
