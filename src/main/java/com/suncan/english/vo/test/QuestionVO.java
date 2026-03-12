package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 题目查询返回对象。
 *
 * 说明：
 * difficulty 已统一为数字编码（1/2/3），前端可直接使用 difficultyCode + difficultyName 展示。
 * 为兼容当前项目已有字段，difficulty 字段继续保留并与 difficultyCode 保持同值。
 */
@Data
@Schema(description = "题目信息")
public class QuestionVO extends BaseQuestionVO {

    @Schema(description = "分值", example = "5")
    private Integer score;

    @Schema(description = "难度编码（兼容字段，与 difficultyCode 同值）", example = "1")
    private Integer difficulty;

    @Schema(description = "难度编码", example = "1")
    private Integer difficultyCode;

    @Schema(description = "难度名称", example = "简单")
    private String difficultyName;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "选项列表")
    private List<QuestionOptionVO> options;
}
