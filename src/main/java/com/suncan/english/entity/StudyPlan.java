package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习计划实体，对应 study_plan 表。
 *
 * 说明：
 * - currentLevel 存的是生成计划时的等级快照编码；
 * - 用 Integer 存储有利于 SQL 统计和索引过滤；
 * - 业务含义通过枚举在 service 层解释。
 */
@Data
@TableName("study_plan")
public class StudyPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /** 学习目标编码：1旅游 2考试 3商务交流 */
    @TableField("goal_type")
    private Integer goalType;

    /** 当前等级快照编码：1初级 2中级 3高级 */
    @TableField("current_level")
    private Integer currentLevel;

    @TableField("daily_minutes")
    private Integer dailyMinutes;

    @TableField("plan_name")
    private String planName;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    /** 计划状态：1进行中 2已结束 */
    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
