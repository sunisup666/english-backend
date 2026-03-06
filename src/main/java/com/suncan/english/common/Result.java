package com.suncan.english.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /** 业务状态码，200 表示成功 */
    private Integer code;
    /** 返回消息 */
    private String message;
    /** 返回数据 */
    private T data;

    /** 成功并返回数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功无数据 */
    public static Result<Void> success() {
        return new Result<>(200, "success", null);
    }

    /** 失败返回 */
    public static Result<Void> fail(String message) {
        return new Result<>(400, message, null);
    }
}
