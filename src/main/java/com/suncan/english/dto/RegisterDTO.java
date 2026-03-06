package com.suncan.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求参数。
 */
@Data
@Schema(description = "用户注册参数")
public class RegisterDTO {
    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "username cannot be blank")
    private String username;

    @Schema(description = "密码(明文传输，后端会做MD5)", example = "123456")
    @NotBlank(message = "password cannot be blank")
    private String password;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@test.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}
