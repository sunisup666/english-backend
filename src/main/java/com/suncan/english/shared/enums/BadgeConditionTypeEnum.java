package com.suncan.english.shared.enums;

import java.util.Arrays;

public enum BadgeConditionTypeEnum {

    CONTINUOUS_STUDY_DAYS(1, "杩炵画缁冧範澶╂暟"),
    TOTAL_PRACTICE_COUNT(2, "绱缁冧範娆℃暟"),
    TOTAL_POINTS(3, "绱绉垎"),
    LEVEL_REACHED(4, "绛夌骇杈惧埌");

    private final Integer code;
    private final String name;

    BadgeConditionTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static BadgeConditionTypeEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getNameByCode(Integer code) {
        BadgeConditionTypeEnum value = fromCode(code);
        return value == null ? "鏈煡" : value.getName();
    }
}


