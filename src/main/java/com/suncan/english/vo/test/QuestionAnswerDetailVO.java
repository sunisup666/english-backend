package com.suncan.english.vo.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 测试详情中的单题作答明细。
 * 继承题目公共字段，再补充“用户答案、标准答案、得分”等详情字段。
 */
@Data
@Schema(description = "单题作答明细")
public class QuestionAnswerDetailVO extends BaseQuestionVO {

    @Schema(description = "用户客观题答案", example = "A")
    private String userAnswer;

    @Schema(description = "用户主观题文本答案")
    private String answerText;

    @Schema(description = "用户语音答案地址")
    private String audioAnswerUrl;

    @Schema(description = "是否正确：1正确 0错误", example = "1")
    private Integer isCorrect;

    @Schema(description = "本题得分", example = "5")
    private Integer score;

    @Schema(description = "标准答案")
    private String standardAnswer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "选项列表")
    private List<QuestionOptionVO> optionList;
}
