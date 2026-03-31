package com.suncan.english.shared.enums;

import java.util.Arrays;

/**
 * 学习目标枚举。
 */
public enum GoalTypeEnum {

    TRAVEL(1, "旅游"),
    EXAM(2, "考试"),
    BUSINESS(3, "商务交流");

    private final Integer code;
    private final String name;

    GoalTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static GoalTypeEnum fromCode(Integer code) {
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }

    public static String getNameByCode(Integer code) {
        GoalTypeEnum value = fromCode(code);
        return value == null ? "未知目标" : value.getName();
    }

    public static boolean containsCode(Integer code) {
        return fromCode(code) != null;
    }
}