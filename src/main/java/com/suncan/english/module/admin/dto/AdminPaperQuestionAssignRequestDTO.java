package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 试卷题目编排保存请求。
 */
@Data
@Schema(description = "试卷题目编排保存请求")
public class AdminPaperQuestionAssignRequestDTO {

    @Schema(description = "题目编排列表")
    private List<AdminPaperQuestionAssignDTO> questions;
}