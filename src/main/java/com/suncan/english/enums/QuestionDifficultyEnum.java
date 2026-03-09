package com.suncan.english.enums;

import java.util.Arrays;

/**
 * 题目难度枚举。
 *
 * 说明：
 * - 当前历史数据可能存数字字符串（"1"/"2"/"3"）或英文（easy/medium/hard）；
 * - 为了最小改动兼容，保留 legacyValue 字段用于识别旧值。
 */
public enum QuestionDifficultyEnum {

    EASY(1, "简单", "easy"),
    MEDIUM(2, "中等", "medium"),
    HARD(3, "困难", "hard");

    private final Integer code;
    private final String name;
    private final String legacyValue;

    QuestionDifficultyEnum(Integer code, String name, String legacyValue) {
        this.code = code;
        this.name = name;
        this.legacyValue = legacyValue;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLegacyValue() {
        return legacyValue;
    }

    public static QuestionDifficultyEnum fromCode(Integer code) {
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }

    public static QuestionDifficultyEnum fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim().toLowerCase();
        for (QuestionDifficultyEnum item : values()) {
            if (item.legacyValue.equals(value) || item.code.toString().equals(value)) {
                return item;
            }
        }
        return null;
    }

    public static String getNameByRaw(String raw) {
        QuestionDifficultyEnum value = fromRaw(raw);
        return value == null ? "未知难度" : value.getName();
    }
}
