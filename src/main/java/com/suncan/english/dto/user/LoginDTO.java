package com.suncan.english.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求参数。
 */
@Data
@Schema(description = "用户登录参数")
public class LoginDTO {

    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "username cannot be blank")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "password cannot be blank")
    private String password;
}