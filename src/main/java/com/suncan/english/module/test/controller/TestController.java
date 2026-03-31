package com.suncan.english.module.test.controller;

import com.suncan.english.module.test.dto.SubmitAnswerDTO;
import com.suncan.english.module.test.dto.TestRecordQueryDTO;
import com.suncan.english.module.test.service.TestService;
import com.suncan.english.module.test.vo.QuestionVO;
import com.suncan.english.module.test.vo.TestRecordDetailVO;
import com.suncan.english.module.test.vo.TestRecordPageVO;
import com.suncan.english.module.test.vo.TestResultVO;
import com.suncan.english.shared.common.Result;
import com.suncan.english.shared.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 测试模块接口。
 */
@Validated
@Tag(name = "用户端-测试模块", description = "语言能力评估与测试接口")
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @Operation(
            summary = "查询试卷题目",
            description = "根据试卷 ID 查询测试题目列表",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/questions")
    public Result<List<QuestionVO>> questions(
            @Parameter(description = "试卷ID", required = true)
            @RequestParam @NotNull(message = "paperId cannot be null") Long paperId) {
        return Result.success(testService.getQuestions(paperId));
    }

    @Operation(
            summary = "提交测试答案",
            description = "提交测试答案并返回测试结果",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/submit")
    public Result<TestResultVO> submit(@Valid @RequestBody SubmitAnswerDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(testService.submitAnswers(userId, dto));
    }

    @Operation(
            summary = "查询测试记录分页",
            description = "分页查询当前登录用户的测试记录",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/page")
    public Result<TestRecordPageVO> recordPage(@Valid TestRecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        return Result.success(testService.queryRecordPage(userId, queryDTO));
    }

    @Operation(
            summary = "查询测试记录详情",
            description = "根据记录 ID 查询测试记录详情",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/{recordId}")
    public Result<TestRecordDetailVO> recordDetail(@PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        return Result.success(testService.recordDetail(userId, recordId));
    }

    @Operation(
            summary = "查询最近一次测试结果",
            description = "查询当前登录用户最近一次测试结果",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/latest")
    public Result<TestResultVO> latest() {
        Long userId = UserContext.getUserId();
        return Result.success(testService.latestResult(userId));
    }
}