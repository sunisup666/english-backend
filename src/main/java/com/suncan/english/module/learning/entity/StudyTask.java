package com.suncan.english.module.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习任务实体，对应 study_task 表。
 */
@Data
@TableName("study_task")
public class StudyTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_id")
    private Long planId;

    @TableField("task_date")
    private LocalDate taskDate;

    @TableField("task_title")
    private String taskTitle;

    @TableField("task_content")
    private String taskContent;

    /**
     * 任务类型：1词汇 2语法 3听力 4口语。
     * 当前阶段已删除阅读任务分支，保持与题库题型一致。
     */
    @TableField("task_type")
    private Integer taskType;

    /**
     * 对应题型编码。
     */
    @TableField("question_type")
    private Integer questionType;

    /**
     * 场景类型：复用场景枚举（通用/旅游/考试/商务）。
     */
    @TableField("scene_type")
    private Integer sceneType;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("task_order")
    private Integer taskOrder;

    /**
     * 任务状态：0未完成 1已完成。
     */
    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}