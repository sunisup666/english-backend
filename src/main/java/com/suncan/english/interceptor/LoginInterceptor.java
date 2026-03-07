package com.suncan.english.interceptor;

import com.suncan.english.config.TokenProperties;
import com.suncan.english.context.UserContext;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：校验 token，并将当前用户写入 UserContext。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final TokenUtil tokenUtil;
    private final TokenProperties tokenProperties;

    public LoginInterceptor(TokenUtil tokenUtil, TokenProperties tokenProperties) {
        this.tokenUtil = tokenUtil;
        this.tokenProperties = tokenProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 先清理一次，避免极端情况下线程复用带来脏数据
        UserContext.clear();

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(tokenProperties.getHeader());
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("请先登录");
        }

        // 兼容 Authorization: Bearer xxx
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = tokenUtil.parseUserId(token);
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束必须清理，避免线程池复用导致上下文串号
        UserContext.clear();
    }
}