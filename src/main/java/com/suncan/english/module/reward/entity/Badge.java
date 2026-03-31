package com.suncan.english.module.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("badge")
public class Badge {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("badge_code")
    private String badgeCode;

    @TableField("badge_name")
    private String badgeName;

    @TableField("description")
    private String description;

    @TableField("icon_url")
    private String iconUrl;

    @TableField("condition_type")
    private Integer conditionType;

    @TableField("condition_value")
    private Integer conditionValue;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}

