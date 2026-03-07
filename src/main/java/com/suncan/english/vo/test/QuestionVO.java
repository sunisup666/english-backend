package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 题目返回对象。
 */
@Data
@Schema(description = "题目信息")
public class QuestionVO {

    @Schema(description = "题目ID", example = "1001")
    private Long questionId;

    @Schema(description = "题目类型", example = "vocabulary_choice")
    private String questionType;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "题目音频地址")
    private String audioUrl;

    @Schema(description = "题目分值", example = "5")
    private Integer score;

    @Schema(description = "题目难度", example = "easy")
    private String difficulty;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "选项列表")
    private List<QuestionOptionVO> options;
}