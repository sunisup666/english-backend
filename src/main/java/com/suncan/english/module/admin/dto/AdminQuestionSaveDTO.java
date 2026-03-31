package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理端题目新增/修改参数。
 */
@Data
@Schema(description = "管理端题目新增或修改参数")
public class AdminQuestionSaveDTO {

    @Schema(description = "题目ID，新增时不传，修改时必传", example = "1")
    private Long id;

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "场景类型编码", example = "3")
    private Integer sceneType;

    @Schema(description = "标题", example = "选择正确的单词")
    private String title;

    @Schema(description = "题干内容", example = "He ____ to school every day.")
    private String content;

    @Schema(description = "音频地址", example = "https://example.com/audio.mp3")
    private String audioUrl;

    @Schema(description = "标准答案", example = "goes")
    private String standardAnswer;

    @Schema(description = "分值", example = "5")
    private Integer score;

    @Schema(description = "难度编码", example = "2")
    private Integer difficulty;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;

    @Schema(description = "解析", example = "主语为第三人称单数，谓语要用 goes")
    private String analysis;

    @Schema(description = "状态：1启用 0禁用", example = "1")
    private Integer status;

    @Schema(description = "选项列表，选择题时传递")
    private List<AdminQuestionOptionDTO> options;
}