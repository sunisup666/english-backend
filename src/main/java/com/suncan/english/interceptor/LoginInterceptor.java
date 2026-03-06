package com.suncan.english.interceptor;

import com.suncan.english.config.TokenProperties;
import com.suncan.english.exception.BusinessException;
import com.suncan.english.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：校验 token，并把当前用户 ID 放到 request 属性中。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER_ID = "currentUserId";

    private final TokenUtil tokenUtil;
    private final TokenProperties tokenProperties;

    public LoginInterceptor(TokenUtil tokenUtil, TokenProperties tokenProperties) {
        this.tokenUtil = tokenUtil;
        this.tokenProperties = tokenProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 不是 Controller 方法时直接放过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(tokenProperties.getHeader());
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("请先登录");
        }

        // 兼容 Bearer xxx 的格式
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = tokenUtil.parseUserId(token);
        request.setAttribute(CURRENT_USER_ID, userId);
        return true;
    }
}
