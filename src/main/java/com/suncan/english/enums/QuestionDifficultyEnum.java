package com.suncan.english.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 题目难度枚举（数字化统一版）。
 *
 * 说明：
 * 1. 当前项目已将 difficulty 统一为数据库数字字段：1简单、2中等、3困难。
 * 2. 因此业务层不再保留 easy/medium/hard 兼容逻辑，避免新旧口径并存造成判断分叉。
 * 3. 枚举只保留最小必要能力：按 code 解析、按 code 取中文名、校验 code 是否有效。
 */
@Getter
@AllArgsConstructor
public enum QuestionDifficultyEnum {

    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final Integer code;
    private final String name;

    public static QuestionDifficultyEnum fromCode(Integer code) {
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }

    public static String getNameByCode(Integer code) {
        QuestionDifficultyEnum value = fromCode(code);
        return value == null ? "未知难度" : value.getName();
    }

    public static boolean containsCode(Integer code) {
        return fromCode(code) != null;
    }
}
