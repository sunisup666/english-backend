package com.suncan.english.shared.security;

import com.suncan.english.shared.config.TokenProperties;
import com.suncan.english.shared.context.AdminContext;
import com.suncan.english.shared.exception.BusinessException;
import com.suncan.english.shared.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员登录拦截器：校验后台 token，并写入 AdminContext。
 */
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    private final TokenUtil tokenUtil;
    private final TokenProperties tokenProperties;

    public AdminLoginInterceptor(TokenUtil tokenUtil, TokenProperties tokenProperties) {
        this.tokenUtil = tokenUtil;
        this.tokenProperties = tokenProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AdminContext.clear();

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(tokenProperties.getHeader());
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("请先登录管理员账号");
        }

        Long adminId = tokenUtil.parseAdminId(token);
        String username = tokenUtil.parseUsername(token);
        AdminContext.setAdmin(adminId, username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AdminContext.clear();
    }
}