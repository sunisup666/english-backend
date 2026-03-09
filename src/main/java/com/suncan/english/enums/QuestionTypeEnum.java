package com.suncan.english.enums;

import java.util.Arrays;

/**
 * 题型枚举。
 */
public enum QuestionTypeEnum {

    VOCABULARY_CHOICE(1, "词汇单选"),
    GRAMMAR_CLOZE(2, "语法填空"),
    LISTENING_CHOICE(3, "听力选择"),
    SPEAKING_SUBJECTIVE(4, "口语主观");

    private final Integer code;
    private final String name;

    QuestionTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static QuestionTypeEnum fromCode(Integer code) {
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }

    public static String getNameByCode(Integer code) {
        QuestionTypeEnum value = fromCode(code);
        return value == null ? "未知题型" : value.getName();
    }

    public static boolean isChoice(Integer code) {
        return VOCABULARY_CHOICE.code.equals(code) || LISTENING_CHOICE.code.equals(code);
    }

    public static boolean isBlank(Integer code) {
        return GRAMMAR_CLOZE.code.equals(code);
    }

    public static boolean isSpeaking(Integer code) {
        return SPEAKING_SUBJECTIVE.code.equals(code);
    }
}
