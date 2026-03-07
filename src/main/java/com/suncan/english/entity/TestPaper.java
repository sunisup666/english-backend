package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试试卷实体，对应 test_paper 表。
 */
@Data
@TableName("test_paper")
public class TestPaper {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("paper_name")
    private String paperName;

    @TableField("description")
    private String description;

    @TableField("total_score")
    private Integer totalScore;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
