package com.suncan.english.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息返回对象。
 *
 * 说明：
 * 1. entity 层只映射数据库真实字段；
 * 2. 给前端展示的扩展字段（如等级中文名）放在 VO，避免污染持久化模型；
 * 3. 这样做可以让“存储结构”和“展示结构”解耦，更适合后续扩展。
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@test.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "英语等级编码：1初级 2中级 3高级", example = "2")
    private Integer englishLevel;

    @Schema(description = "英语等级中文名称", example = "中级")
    private String englishLevelName;
}