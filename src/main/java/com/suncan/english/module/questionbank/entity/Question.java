package com.suncan.english.module.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目实体，对应 question 表。
 *
 * 说明：
 * 1. question 表已不再直接绑定试卷，不再维护 paper_id；
 * 2. 试卷与题目关系统一由 paper_question 中间表维护；
 * 3. difficulty 使用数字编码：1简单、2中等、3困难。
 */
@Data
@TableName("question")
public class Question {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("question_type")
    private Integer questionType;

    @TableField("scene_type")
    private Integer sceneType;

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
    private Integer difficulty;

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