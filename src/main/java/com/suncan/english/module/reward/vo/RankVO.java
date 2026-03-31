package com.suncan.english.module.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "排行榜")
public class RankVO {

    @Schema(description = "排行榜列表")
    private List<RankItemVO> rankList;

    @Schema(description = "我的排名", example = "8")
    private Integer myRank;

    @Schema(description = "我的积分", example = "120")
    private Integer myPoints;
}