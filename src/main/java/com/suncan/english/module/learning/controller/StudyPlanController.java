package com.suncan.english.module.learning.controller;

import com.suncan.english.module.learning.dto.GenerateStudyPlanDTO;
import com.suncan.english.module.learning.dto.StudyPlanListQueryDTO;
import com.suncan.english.module.learning.dto.StudyTaskQueryDTO;
import com.suncan.english.module.learning.dto.UpdateStudyTaskStatusDTO;
import com.suncan.english.module.learning.service.StudyPlanService;
import com.suncan.english.module.learning.vo.StudyPlanPageVO;
import com.suncan.english.module.learning.vo.StudyPlanVO;
import com.suncan.english.module.learning.vo.StudyTaskPageVO;
import com.suncan.english.module.learning.vo.StudyTaskVO;
import com.suncan.english.shared.common.Result;
import com.suncan.english.shared.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 学习计划模块接口。
 */
@Validated
@Tag(name = "用户端-学习计划模块", description = "学习计划与任务相关接口")
@RestController
@RequestMapping("/api/plan")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @Operation(
            summary = "生成学习计划",
            description = "根据用户输入的目标和参数生成新的学习计划",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/generate")
    public Result<StudyPlanVO> generate(@Valid @RequestBody GenerateStudyPlanDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.generateStudyPlan(userId, dto));
    }

    @Operation(
            summary = "查询当前学习计划",
            description = "查询当前登录用户正在进行中的学习计划",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/current")
    public Result<StudyPlanVO> current() {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.getCurrentPlan(userId));
    }

    @Operation(
            summary = "查询学习计划列表",
            description = "分页查询当前登录用户的学习计划集合，可按状态筛选",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/list")
    public Result<StudyPlanPageVO> listPlans(StudyPlanListQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.listPlans(userId, queryDTO));
    }

    @Operation(
            summary = "查询计划内任务列表",
            description = "根据学习计划 ID 查询该计划下的任务列表，可按日期、状态和任务类型筛选",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/{planId}/tasks")
    public Result<StudyTaskPageVO> listPlanTasks(
            @Parameter(description = "学习计划ID", required = true)
            @PathVariable Long planId,
            @Parameter(description = "任务日期，格式为 yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate taskDate,
            @Parameter(description = "任务状态")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "任务类型")
            @RequestParam(required = false) Integer taskType,
            @Parameter(description = "当前页")
            @RequestParam(required = false) Long current,
            @Parameter(description = "每页大小")
            @RequestParam(required = false) Long size) {
        Long userId = UserContext.getUserId();

        StudyTaskQueryDTO queryDTO = new StudyTaskQueryDTO();
        queryDTO.setPlanId(planId);
        queryDTO.setTaskDate(taskDate);
        queryDTO.setStatus(status);
        queryDTO.setTaskType(taskType);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        return Result.success(studyPlanService.listPlanTasks(userId, queryDTO));
    }

    @Operation(
            summary = "查询任务详情",
            description = "根据任务 ID 查询学习计划任务详情",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/task/detail/{taskId}")
    public Result<StudyTaskVO> taskDetail(@PathVariable Long taskId) {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.taskDetail(userId, taskId));
    }

    @Operation(
            summary = "更新任务状态",
            description = "更新学习任务的完成状态",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PutMapping("/task/status")
    public Result<Void> updateTaskStatus(@Valid @RequestBody UpdateStudyTaskStatusDTO dto) {
        Long userId = UserContext.getUserId();
        studyPlanService.updateTaskStatus(userId, dto);
        return Result.success();
    }
}