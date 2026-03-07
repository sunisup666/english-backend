package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目选项返回对象。
 */
@Data
@Schema(description = "题目选项")
public class QuestionOptionVO {

    @Schema(description = "选项标识", example = "A")
    private String optionLabel;

    @Schema(description = "选项内容", example = "I go to school by bus.")
    private String optionContent;

    @Schema(description = "选项排序", example = "1")
    private Integer sortOrder;
}