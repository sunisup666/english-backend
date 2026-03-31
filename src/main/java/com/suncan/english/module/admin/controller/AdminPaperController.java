package com.suncan.english.module.admin.controller;

import com.suncan.english.module.admin.dto.AdminPaperQueryDTO;
import com.suncan.english.module.admin.dto.AdminPaperQuestionAssignRequestDTO;
import com.suncan.english.module.admin.dto.AdminPaperSaveDTO;
import com.suncan.english.module.admin.service.AdminPaperService;
import com.suncan.english.module.admin.vo.AdminPaperPageVO;
import com.suncan.english.module.admin.vo.AdminPaperQuestionVO;
import com.suncan.english.module.admin.vo.AdminPaperVO;
import com.suncan.english.shared.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端试卷管理接口。
 */
@Tag(name = "管理端-试卷管理", description = "管理员后台试卷分页查询、详情、新增、修改、删除与题目编排接口")
@RestController
@RequestMapping("/api/admin/paper")
public class AdminPaperController {

    private final AdminPaperService adminPaperService;

    public AdminPaperController(AdminPaperService adminPaperService) {
        this.adminPaperService = adminPaperService;
    }

    @Operation(
            summary = "分页查询试卷",
            description = "分页查询试卷，支持按试卷名称关键词和状态筛选",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/page")
    public Result<AdminPaperPageVO> page(AdminPaperQueryDTO queryDTO) {
        return Result.success(adminPaperService.page(queryDTO));
    }

    @Operation(
            summary = "查询试卷详情",
            description = "根据试卷ID查询试卷基本信息",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/{id}")
    public Result<AdminPaperVO> detail(
            @Parameter(description = "试卷ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        return Result.success(adminPaperService.detail(id));
    }

    @Operation(
            summary = "新增试卷",
            description = "新增试卷基础信息，不包含自动组卷逻辑",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminPaperSaveDTO dto) {
        adminPaperService.create(dto);
        return Result.success();
    }

    @Operation(
            summary = "修改试卷",
            description = "根据试卷ID修改试卷基础信息",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AdminPaperSaveDTO dto) {
        adminPaperService.update(dto);
        return Result.success();
    }

    @Operation(
            summary = "删除试卷",
            description = "根据试卷ID删除试卷，并同步清理题目关联关系",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "试卷ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        adminPaperService.delete(id);
        return Result.success();
    }

    @Operation(
            summary = "查询试卷题目编排",
            description = "查询指定试卷下的题目关联信息，返回题目ID、分值、排序值以及题目基础信息",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/{paperId}/questions")
    public Result<List<AdminPaperQuestionVO>> listQuestions(
            @Parameter(description = "试卷ID", required = true, example = "1")
            @PathVariable Long paperId
    ) {
        return Result.success(adminPaperService.listQuestions(paperId));
    }

    @Operation(
            summary = "保存试卷题目编排",
            description = "按 questionId、score、sortOrder 维护试卷题目关联关系，本次保存将覆盖原有编排",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/{paperId}/questions")
    public Result<Void> saveQuestions(
            @Parameter(description = "试卷ID", required = true, example = "1")
            @PathVariable Long paperId,
            @Valid @RequestBody AdminPaperQuestionAssignRequestDTO dto
    ) {
        adminPaperService.saveQuestions(paperId, dto);
        return Result.success();
    }
}