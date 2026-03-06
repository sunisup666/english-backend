package com.suncan.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库 user 表。
 */
@Data
@Schema(description = "用户信息")
@TableName("`user`")
public class User {
    @Schema(description = "主键ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    @TableField("username")
    private String username;

    @Schema(description = "密码（返回时一般为空）")
    @TableField("password")
    private String password;

    @Schema(description = "昵称", example = "张三")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@test.com")
    @TableField("email")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @TableField("phone")
    private String phone;

    @Schema(description = "英语等级", example = "初级")
    @TableField("english_level")
    private String englishLevel;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
