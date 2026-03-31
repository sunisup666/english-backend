package com.suncan.english.module.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "排行榜项")
public class RankItemVO {

    @Schema(description = "排名", example = "1")
    private Integer rank;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "总积分", example = "500")
    private Integer totalPoints;
}