package com.suncan.english.module.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "积分信息")
public class PointsVO {

    @Schema(description = "总积分", example = "120")
    private Integer totalPoints;
}