package com.suncan.english.enums;

/**
 * 学习计划状态枚举。
 */
public enum StudyPlanStatusEnum {

    RUNNING(1, "进行中"),
    FINISHED(2, "已完成");

    private final Integer code;
    private final String name;

    StudyPlanStatusEnum(Integer code, String name) {
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
