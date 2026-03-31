package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端用户分页查询参数。
 */
@Data
@Schema(description = "管理端用户分页查询参数")
public class AdminUserPageQueryDTO {

    @Schema(description = "关键词，匹配用户名、昵称、手机号、邮箱", example = "zhangsan")
    private String keyword;

    @Schema(description = "英语等级编码", example = "2")
    private Integer englishLevel;

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;
}