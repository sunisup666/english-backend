package com.suncan.english.util;

import com.suncan.english.enums.EnglishLevelEnum;

/**
 * 英语等级工具类。
 *
 * 迁移说明：
 * - 现阶段主要是兼容旧调用点；
 * - 内部已切换为 EnglishLevelEnum 实现；
 * - 新代码优先直接使用枚举。
 */
public class EnglishLevelUtil {

    private EnglishLevelUtil() {
    }

    public static String getLevelName(Integer level) {
        return EnglishLevelEnum.getNameByCode(level);
    }

    public static boolean isValid(Integer level) {
        return EnglishLevelEnum.containsCode(level);
    }
}
