package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户答题明细实体，对应 user_test_answer 表。
 */
@Data
@TableName("user_test_answer")
public class UserTestAnswer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("record_id")
    private Long recordId;

    @TableField("question_id")
    private Long questionId;

    @TableField("user_answer")
    private String userAnswer;

    @TableField("is_correct")
    private Integer isCorrect;

    @TableField("score")
    private Integer score;

    @TableField("answer_text")
    private String answerText;

    @TableField("audio_answer_url")
    private String audioAnswerUrl;

    @TableField("create_time")
    private LocalDateTime createTime;
}
