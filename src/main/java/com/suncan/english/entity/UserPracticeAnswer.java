package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户学习任务练习作答明细实体，对应 user_practice_answer。
 */
@Data
@TableName("user_practice_answer")
public class UserPracticeAnswer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("practice_record_id")
    private Long practiceRecordId;

    @TableField("question_id")
    private Long questionId;

    @TableField("user_answer")
    private String userAnswer;

    @TableField("answer_text")
    private String answerText;

    @TableField("audio_answer_url")
    private String audioAnswerUrl;

    @TableField("is_correct")
    private Integer isCorrect;

    @TableField("score")
    private Integer score;

    @TableField("create_time")
    private LocalDateTime createTime;
}
