package com.suncan.english.module.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户测试记录实体，对应 user_test_record 表。
 *
 * 说明：
 * - levelResult 为测试计算后的等级编码（1初级 2中级 3高级）；
 * - entity 只保存编码，不保存中文名；
 * - 中文展示统一在 VO 组装阶段由枚举转换。
 */
@Data
@TableName("user_test_record")
public class UserTestRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("paper_id")
    private Long paperId;

    @TableField("total_score")
    private Integer totalScore;

    @TableField("correct_count")
    private Integer correctCount;

    /** 测试等级结果编码：1初级 2中级 3高级 */
    @TableField("level_result")
    private Integer levelResult;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("create_time")
    private LocalDateTime createTime;
}