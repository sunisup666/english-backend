package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目选项实体，对应 question_option 表。
 */
@Data
@TableName("question_option")
public class QuestionOption {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("question_id")
    private Long questionId;

    @TableField("option_label")
    private String optionLabel;

    @TableField("option_content")
    private String optionContent;

    @TableField("is_correct")
    private Integer isCorrect;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_time")
    private LocalDateTime createTime;
}
