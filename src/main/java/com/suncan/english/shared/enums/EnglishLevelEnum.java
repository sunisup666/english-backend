package com.suncan.english.shared.enums;

import java.util.Arrays;

/**
 * 英语等级枚举。
 *
 * 业务语义：
 * - code 用于数据库存储、后端判断、前端逻辑分支；
 * - name 用于页面展示。
 *
 * 为什么使用枚举：
 * - 避免在业务代码里散落 1/2/3 魔法值；
 * - 避免字符串文案变化影响判断逻辑；
 * - 统一“编码 -> 名称”转换入口。
 */
public enum EnglishLevelEnum {

    BEGINNER(1, "初级"),
    INTERMEDIATE(2, "中级"),
    ADVANCED(3, "高级");

    private final Integer code;
    private final String name;

    EnglishLevelEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static EnglishLevelEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getNameByCode(Integer code) {
        EnglishLevelEnum value = fromCode(code);
        return value == null ? "未知" : value.getName();
    }

    public static boolean containsCode(Integer code) {
        return fromCode(code) != null;
    }
}