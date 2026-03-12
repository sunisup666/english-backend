package com.suncan.english.vo.practice;

import com.suncan.english.vo.test.QuestionOptionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 训练记录详情中的单题作答明细。
 */
@Data
@Schema(description = "训练记录单题作答明细")
public class PracticeQuestionAnswerDetailVO {

    @Schema(description = "题目ID", example = "1001")
    private Long questionId;

    @Schema(description = "题型编码", example = "1")
    private Integer questionType;

    @Schema(description = "题型名称", example = "词汇单选")
    private String questionTypeName;

    @Schema(description = "场景编码", example = "2")
    private Integer sceneType;

    @Schema(description = "场景名称", example = "旅游")
    private String sceneTypeName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "音频地址")
    private String audioUrl;

    @Schema(description = "选项列表（仅选择题有值）")
    private List<QuestionOptionVO> optionList;

    @Schema(description = "用户答案")
    private String userAnswer;

    @Schema(description = "用户文本答案")
    private String answerText;

    @Schema(description = "用户音频答案地址")
    private String audioAnswerUrl;

    @Schema(description = "是否正确", example = "1")
    private Integer isCorrect;

    @Schema(description = "本题得分", example = "5")
    private Integer score;

    @Schema(description = "标准答案")
    private String standardAnswer;

    @Schema(description = "题目解析")
    private String analysis;
}
