package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户学习任务练习记录主表实体，对应 user_practice_record。
 *
 * 说明：
 * 1. 本表与 user_test_record 分开存储，避免“能力测试”与“日常任务练习”语义混淆。
 * 2. 本表强调任务上下文（planId/taskId/taskType），用于学习计划闭环统计。
 * 3. 当前阶段 status 先使用简单状态：1=已提交，后续如需“进行中/中断”可扩展。
 */
@Data
@TableName("user_practice_record")
public class UserPracticeRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("plan_id")
    private Long planId;

    @TableField("task_id")
    private Long taskId;

    @TableField("task_type")
    private Integer taskType;

    @TableField("question_type")
    private Integer questionType;

    @TableField("scene_type")
    private Integer sceneType;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("correct_count")
    private Integer correctCount;

    @TableField("total_score")
    private Integer totalScore;

    @TableField("status")
    private Integer status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
