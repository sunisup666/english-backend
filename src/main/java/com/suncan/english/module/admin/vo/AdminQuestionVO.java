package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端题目返回对象。
 */
@Data
@Schema(description = "管理端题目信息")
public class AdminQuestionVO {

    @Schema(description = "题目ID", example = "1")
    private Long id;

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "题型名称", example = "词汇单选")
    private String questionTypeName;

    @Schema(description = "场景类型编码", example = "3")
    private Integer sceneType;

    @Schema(description = "场景类型名称", example = "考试")
    private String sceneTypeName;

    @Schema(description = "标题", example = "选择正确的单词")
    private String title;

    @Schema(description = "题干内容", example = "He ____ to school every day.")
    private String content;

    @Schema(description = "音频地址")
    private String audioUrl;

    @Schema(description = "标准答案", example = "goes")
    private String standardAnswer;

    @Schema(description = "分值", example = "5")
    private Integer score;

    @Schema(description = "难度编码", example = "2")
    private Integer difficulty;

    @Schema(description = "难度名称", example = "中等")
    private String difficultyName;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "状态：1启用 0禁用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "选项列表")
    private List<AdminQuestionOptionVO> options;
}