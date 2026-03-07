package com.suncan.english.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人信息请求参数。
 */
@Data
@Schema(description = "修改个人信息参数")
public class UpdateUserDTO {

    @Schema(description = "昵称", example = "张三同学")
    @Size(max = 50, message = "nickname length must be <= 50")
    private String nickname;

    @Schema(description = "邮箱", example = "new_mail@test.com")
    @Email(message = "email format is invalid")
    @Size(max = 100, message = "email length must be <= 100")
    private String email;

    @Schema(description = "手机号", example = "13900139000")
    @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "phone format is invalid")
    private String phone;
}