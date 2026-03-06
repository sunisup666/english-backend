package com.suncan.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求参数。
 */
@Data
@Schema(description = "修改密码参数")
public class UpdatePasswordDTO {
    @Schema(description = "旧密码", example = "123456")
    @NotBlank(message = "oldPassword cannot be blank")
    private String oldPassword;

    @Schema(description = "新密码", example = "12345678")
    @NotBlank(message = "newPassword cannot be blank")
    private String newPassword;
}
