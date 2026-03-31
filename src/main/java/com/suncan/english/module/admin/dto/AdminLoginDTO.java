package com.suncan.english.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录参数。
 */
@Data
@Schema(description = "管理员登录参数")
public class AdminLoginDTO {

    @Schema(description = "管理员账号", example = "admin")
    @NotBlank(message = "username cannot be blank")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "password cannot be blank")
    private String password;
}