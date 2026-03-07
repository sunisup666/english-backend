package com.suncan.english.context;

import com.suncan.english.exception.BusinessException;

/**
 * 当前登录用户上下文。
 * 使用 ThreadLocal 在一次请求生命周期内保存 userId。
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        Long userId = USER_ID_HOLDER.get();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return userId;
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}

