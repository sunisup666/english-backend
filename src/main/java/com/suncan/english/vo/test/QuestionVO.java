package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询题目返回对象。
 * 继承题目公共字段，只保留当前场景特有字段。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "题目信息")
public class QuestionVO extends BaseQuestionVO {

    @Schema(description = "题目分值", example = "5")
    private Integer score;

    @Schema(description = "题目难度", example = "easy")
    private String difficulty;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "选项列表")
    private List<QuestionOptionVO> options;
}
