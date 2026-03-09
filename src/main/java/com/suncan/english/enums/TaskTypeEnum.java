package com.suncan.english.enums;

import java.util.Arrays;

/**
 * 学习任务类型枚举。
 */
public enum TaskTypeEnum {

    VOCABULARY(1, "词汇"),
    GRAMMAR(2, "语法"),
    LISTENING(3, "听力"),
    SPEAKING(4, "口语"),
    READING(5, "阅读");

    private final Integer code;
    private final String name;

    TaskTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static TaskTypeEnum fromCode(Integer code) {
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }

    public static String getNameByCode(Integer code) {
        TaskTypeEnum value = fromCode(code);
        return value == null ? "未知任务" : value.getName();
    }
}
