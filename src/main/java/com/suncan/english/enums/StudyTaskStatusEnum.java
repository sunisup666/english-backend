package com.suncan.english.enums;

/**
 * 学习任务状态枚举。
 */
public enum StudyTaskStatusEnum {

    TODO(0, "待完成"),
    DONE(1, "已完成");

    private final Integer code;
    private final String name;

    StudyTaskStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
