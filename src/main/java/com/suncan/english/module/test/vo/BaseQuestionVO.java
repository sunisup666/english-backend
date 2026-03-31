package com.suncan.english.module.test.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目返回基础字段。
 *
 * 说明：
 * - questionType/sceneType 保留编码，便于前端逻辑判断；
 * - questionTypeName/sceneTypeName 提供中文名，便于页面直接展示。
 */
@Data
public class BaseQuestionVO {

    @Schema(description = "题目ID", example = "1001")
    private Long questionId;

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "题型名称", example = "词汇单选")
    private String questionTypeName;

    @Schema(description = "场景编码", example = "1")
    private Integer sceneType;

    @Schema(description = "场景名称", example = "通用")
    private String sceneTypeName;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "音频地址")
    private String audioUrl;
}