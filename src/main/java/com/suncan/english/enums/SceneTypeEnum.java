package com.suncan.english.enums;

import java.util.Arrays;

/**
 * 场景类型枚举。
 */
public enum SceneTypeEnum {

    GENERAL(1, "通用"),
    TRAVEL(2, "旅游"),
    EXAM(3, "考试"),
    BUSINESS(4, "商务");

    private final Integer code;
    private final String name;

    SceneTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static SceneTypeEnum fromCode(Integer code) {
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }

    public static String getNameByCode(Integer code) {
        SceneTypeEnum value = fromCode(code);
        return value == null ? "未知场景" : value.getName();
    }
}
