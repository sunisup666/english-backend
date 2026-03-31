package com.suncan.english.shared.config;

import com.suncan.english.shared.security.AdminLoginInterceptor;
import com.suncan.english.shared.security.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置，注册登录拦截器。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final AdminLoginInterceptor adminLoginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor, AdminLoginInterceptor adminLoginInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.adminLoginInterceptor = adminLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/user/**",
                        "/api/test/**",
                        "/api/plan/**",
                        "/api/practice/**",
                        "/api/reward/**",
                        "/api/progress/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );

        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns(
                        "/api/admin/login",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );
    }
}