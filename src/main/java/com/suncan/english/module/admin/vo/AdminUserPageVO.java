package com.suncan.english.module.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理端用户分页结果。
 *
 * 用于后台用户列表页，强调分页元信息与列表记录，不包含完整学习画像明细。
 */
@Data
@Schema(description = "管理端用户分页列表结果")
public class AdminUserPageVO {

    @Schema(description = "当前页", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    @Schema(description = "总记录数", example = "25")
    private Long total;

    @Schema(description = "用户分页列表记录")
    private List<AdminUserPageItemVO> records;
}