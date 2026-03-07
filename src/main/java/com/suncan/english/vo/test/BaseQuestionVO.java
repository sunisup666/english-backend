package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目通用字段。
 * 用于复用题目基础信息，避免查询题目和记录详情两个 VO 重复定义相同字段。
 */
@Data
public class BaseQuestionVO {

    @Schema(description = "题目ID", example = "1001")
    private Long questionId;

    @Schema(description = "题目类型：1词汇单选 2语法填空 3听力选择 4口语主观", example = "1")
    private Integer questionType;

    @Schema(description = "场景类型：1通用 2旅游 3考试 4商务交流", example = "1")
    private Integer sceneType;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "题目音频地址")
    private String audioUrl;
}

