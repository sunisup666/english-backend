package com.suncan.english.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学习任务类型枚举。
 *
 * 说明：
 * 1. 当前阶段仅保留与题库题型一致的 4 类任务：词汇、语法、听力、口语。
 * 2. 删除阅读任务分支的原因是：题库尚无阅读题型，保留会导致“任务可见但无法正常出题”的假功能。
 * 3. 后续若阅读题型正式入库，再同步恢复阅读任务分支更合理，能保证功能上线即闭环。
 */
@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    VOCABULARY(1, "词汇"),
    GRAMMAR(2, "语法"),
    LISTENING(3, "听力"),
    SPEAKING(4, "口语");

    private final Integer code;
    private final String name;

    public static TaskTypeEnum fromCode(Integer code) {
        return Arrays.stream(values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
    }

    public static String getNameByCode(Integer code) {
        TaskTypeEnum value = fromCode(code);
        return value == null ? "未知任务" : value.getName();
    }
}