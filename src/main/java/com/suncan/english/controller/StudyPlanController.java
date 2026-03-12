package com.suncan.english.controller;

import com.suncan.english.common.Result;
import com.suncan.english.context.UserContext;
import com.suncan.english.dto.plan.GenerateStudyPlanDTO;
import com.suncan.english.dto.plan.StudyTaskQueryDTO;
import com.suncan.english.dto.plan.UpdateStudyTaskStatusDTO;
import com.suncan.english.service.StudyPlanService;
import com.suncan.english.vo.plan.StudyPlanVO;
import com.suncan.english.vo.plan.StudyTaskPageVO;
import com.suncan.english.vo.plan.StudyTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@Tag(name = "学习计划模块", description = "个性化学习计划与任务管理接口")
@RestController
@RequestMapping("/api/plan")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    /**
     * 生成学习计划。
     */
    @Operation(
            summary = "生成学习计划",
            description = "根据学习目标、当前等级、每日学习时长生成计划和任务",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/generate")
    public Result<StudyPlanVO> generate(@Valid @RequestBody GenerateStudyPlanDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.generateStudyPlan(userId, dto));
    }

    /**
     * 查询当前进行中的学习计划。
     */
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

    /**
     * 查询计划任务列表（统一分页 + 条件查询接口）。
     *
     * 设计说明：
     * 1. 保留原路径不变，前端无需因为“今天/全部/已完成”等视图切换而对接多个列表接口。
     * 2. 统一采用“分页 + 条件查询”，后续新增筛选项时只扩展参数，不需要继续新增 today/all/finished 子接口。
     * 3. taskDate/status/taskType 设计为可选参数，前端可以按页面场景灵活组合：
     *    传 taskDate=今天可看当天任务，不传 taskDate 可看全部任务，再叠加状态/类型筛选即可。
     */
    @Operation(
            summary = "查询计划任务列表",
            description = "按计划ID分页查询任务列表，支持按日期/状态/任务类型筛选",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/task/list")
    public Result<StudyTaskPageVO> taskList(
            @Parameter(description = "计划ID", required = true)
            @RequestParam @NotNull(message = "planId cannot be null") Long planId,
            @Parameter(description = "任务日期（可选）")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate taskDate,
            @Parameter(description = "任务状态（可选）：0未完成 1已完成")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "任务类型（可选）：1词汇 2语法 3听力 4口语")
            @RequestParam(required = false) Integer taskType,
            @Parameter(description = "当前页（可选，默认1）")
            @RequestParam(required = false) Long current,
            @Parameter(description = "每页条数（可选，默认10，最大50）")
            @RequestParam(required = false) Long size) {
        Long userId = UserContext.getUserId();

        // 控制层只负责接参与封装，不承载业务校验，避免与 service 层重复校验逻辑。
        StudyTaskQueryDTO queryDTO = new StudyTaskQueryDTO();
        queryDTO.setPlanId(planId);
        queryDTO.setTaskDate(taskDate);
        queryDTO.setStatus(status);
        queryDTO.setTaskType(taskType);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        return Result.success(studyPlanService.listPlanTasks(userId, queryDTO));
    }

    /**
     * 查询单个学习任务详情（仅任务元数据）。
     *
     * 说明：
     * 任务详情接口只返回任务本身信息，不加载练习题目；
     * 练习题目由 Practice 模块的“任务练习内容接口”单独返回，职责边界更清晰。
     */
    @Operation(
            summary = "查询学习任务详情",
            description = "返回任务元数据详情，不包含练习题目",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/task/detail/{taskId}")
    public Result<StudyTaskVO> taskDetail(@PathVariable Long taskId) {
        Long userId = UserContext.getUserId();
        return Result.success(studyPlanService.taskDetail(userId, taskId));
    }

    /**
     * 更新任务完成状态。
     */
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
