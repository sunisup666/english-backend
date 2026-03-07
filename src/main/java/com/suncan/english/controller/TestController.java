package com.suncan.english.controller;

import com.suncan.english.common.Result;
import com.suncan.english.context.UserContext;
import com.suncan.english.dto.test.SubmitAnswerDTO;
import com.suncan.english.dto.test.TestRecordQueryDTO;
import com.suncan.english.service.TestService;
import com.suncan.english.vo.test.QuestionVO;
import com.suncan.english.vo.test.TestRecordDetailVO;
import com.suncan.english.vo.test.TestRecordPageVO;
import com.suncan.english.vo.test.TestResultVO;
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
 * 语言能力评估与测试接口。
 */
@Validated
@Tag(name = "测试模块", description = "语言能力评估与测试接口")
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @Operation(
            summary = "查询试卷题目",
            description = "根据 paperId 查询题目，返回内容不包含标准答案",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/questions")
    public Result<List<QuestionVO>> questions(
            @Parameter(description = "试卷ID", required = true)
            @RequestParam @NotNull(message = "paperId cannot be null") Long paperId) {
        return Result.success(testService.getQuestions(paperId));
    }

    @Operation(
            summary = "提交答案",
            description = "提交后自动判分并保存测试记录与答题明细",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping("/submit")
    public Result<TestResultVO> submit(@Valid @RequestBody SubmitAnswerDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(testService.submitAnswers(userId, dto));
    }

    @Operation(
            summary = "分页查询测试记录",
            description = "记录中心接口，返回测试记录摘要列表",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/page")
    public Result<TestRecordPageVO> recordPage(@Valid TestRecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        return Result.success(testService.queryRecordPage(userId, queryDTO));
    }

    @Operation(
            summary = "查询测试记录详情",
            description = "根据 recordId 查询测试记录详情和逐题作答明细",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/record/{recordId}")
    public Result<TestRecordDetailVO> recordDetail(@PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        return Result.success(testService.recordDetail(userId, recordId));
    }

    @Operation(
            summary = "查询最近一次测试结果摘要",
            description = "首页快捷接口，仅返回最近一次记录摘要",
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @GetMapping("/latest")
    public Result<TestResultVO> latest() {
        Long userId = UserContext.getUserId();
        return Result.success(testService.latestResult(userId));
    }
}