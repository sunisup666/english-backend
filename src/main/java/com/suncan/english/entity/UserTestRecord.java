package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户测试记录实体，对应 user_test_record 表。
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

    @TableField("level_result")
    private String levelResult;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("create_time")
    private LocalDateTime createTime;
}
