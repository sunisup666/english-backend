package com.suncan.english.module.admin.controller;

import com.suncan.english.module.admin.dto.AdminQuestionQueryDTO;
import com.suncan.english.module.admin.dto.AdminQuestionSaveDTO;
import com.suncan.english.module.admin.service.AdminQuestionService;
import com.suncan.english.module.admin.vo.AdminQuestionPageVO;
import com.suncan.english.module.admin.vo.AdminQuestionVO;
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

/**
 * 管理端题库管理接口。
 */
@Tag(name = "管理端-题库管理", description = "管理员后台题目分页查询、详情、新增、修改和删除接口")
@RestController
@RequestMapping("/api/admin/question")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @Operation(
            summary = "分页查询题目",
            description = "分页查询题库题目，支持按题型、场景类型、难度、状态和标题关键词筛选",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/page")
    public Result<AdminQuestionPageVO> page(AdminQuestionQueryDTO queryDTO) {
        return Result.success(adminQuestionService.page(queryDTO));
    }

    @Operation(
            summary = "查询题目详情",
            description = "根据题目ID查询题目详情及其选项信息",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/{id}")
    public Result<AdminQuestionVO> detail(
            @Parameter(description = "题目ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        return Result.success(adminQuestionService.detail(id));
    }

    @Operation(
            summary = "新增题目",
            description = "新增题库题目。选择题可同时保存选项，非选择题可不传选项。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminQuestionSaveDTO dto) {
        adminQuestionService.create(dto);
        return Result.success();
    }

    @Operation(
            summary = "修改题目",
            description = "根据题目ID修改题目信息，并按传入内容更新选项。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AdminQuestionSaveDTO dto) {
        adminQuestionService.update(dto);
        return Result.success();
    }

    @Operation(
            summary = "删除题目",
            description = "根据题目ID删除题目，同时清理其选项和试卷关联关系。",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "题目ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        adminQuestionService.delete(id);
        return Result.success();
    }
}