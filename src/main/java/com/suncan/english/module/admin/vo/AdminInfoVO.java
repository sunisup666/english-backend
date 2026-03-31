package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员信息返回对象。
 */
@Data
@Schema(description = "管理员信息")
public class AdminInfoVO {

    @Schema(description = "管理员ID", example = "1")
    private Long id;

    @Schema(description = "管理员账号", example = "admin")
    private String username;

    @Schema(description = "管理员昵称", example = "系统管理员")
    private String nickname;

    @Schema(description = "状态：1启用 0禁用", example = "1")
    private Integer status;
}