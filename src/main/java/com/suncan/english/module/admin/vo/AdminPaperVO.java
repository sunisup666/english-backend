package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端试卷返回对象。
 */
@Data
@Schema(description = "管理端试卷信息")
public class AdminPaperVO {

    @Schema(description = "试卷ID", example = "1")
    private Long id;

    @Schema(description = "试卷名称", example = "英语基础测试卷")
    private String paperName;

    @Schema(description = "试卷描述")
    private String description;

    @Schema(description = "总分", example = "100")
    private Integer totalScore;

    @Schema(description = "时长（分钟）", example = "30")
    private Integer durationMinutes;

    @Schema(description = "状态：1启用 0禁用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}