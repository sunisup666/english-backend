package com.suncan.english.module.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "徽章信息")
public class BadgeVO {

    @Schema(description = "徽章ID", example = "1")
    private Long badgeId;

    @Schema(description = "徽章编码", example = "CONTINUOUS_3")
    private String badgeCode;

    @Schema(description = "徽章名称", example = "连续学习")
    private String badgeName;

    @Schema(description = "徽章描述", example = "连续学习3天")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "条件类型", example = "1")
    private Integer conditionType;

    @Schema(description = "条件类型名称", example = "连续学习天数")
    private String conditionTypeName;

    @Schema(description = "条件值", example = "3")
    private Integer conditionValue;

    @Schema(description = "是否获得", example = "1")
    private Integer isEarned;

    @Schema(description = "获得时间")
    private LocalDateTime earnedTime;
}