package com.suncan.english.shared.context;

import com.suncan.english.shared.exception.BusinessException;

/**
 * 当前登录管理员上下文。
 */
public class AdminContext {

    private static final ThreadLocal<Long> ADMIN_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> ADMIN_USERNAME_HOLDER = new ThreadLocal<>();

    private AdminContext() {
    }

    public static void setAdmin(Long adminId, String username) {
        ADMIN_ID_HOLDER.set(adminId);
        ADMIN_USERNAME_HOLDER.set(username);
    }

    public static Long getAdminId() {
        Long adminId = ADMIN_ID_HOLDER.get();
        if (adminId == null) {
            throw new BusinessException("请先登录管理员账号");
        }
        return adminId;
    }

    public static String getUsername() {
        return ADMIN_USERNAME_HOLDER.get();
    }

    public static void clear() {
        ADMIN_ID_HOLDER.remove();
        ADMIN_USERNAME_HOLDER.remove();
    }
}