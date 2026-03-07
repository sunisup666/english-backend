package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目实体，对应 question 表。
 */
@Data
@TableName("question")
public class Question {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("paper_id")
    private Long paperId;

    @TableField("question_type")
    private String questionType;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("audio_url")
    private String audioUrl;

    @TableField("standard_answer")
    private String standardAnswer;

    @TableField("score")
    private Integer score;

    @TableField("difficulty")
    private String difficulty;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("analysis")
    private String analysis;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
