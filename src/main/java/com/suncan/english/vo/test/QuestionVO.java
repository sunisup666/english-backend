package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 试卷题目查询返回对象。
 */
@Data
@Schema(description = "题目信息")
public class QuestionVO extends BaseQuestionVO {

    @Schema(description = "分值", example = "5")
    private Integer score;

    @Schema(description = "难度原始值（数据库存储）", example = "easy")
    private String difficulty;

    @Schema(description = "难度编码（若可解析）", example = "1")
    private Integer difficultyCode;

    @Schema(description = "难度名称", example = "简单")
    private String difficultyName;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "选项列表")
    private List<QuestionOptionVO> options;
}
