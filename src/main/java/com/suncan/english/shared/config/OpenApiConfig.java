package com.suncan.english.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 基础配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String schemeName = "Authorization";
        return new OpenAPI()
                .info(new Info()
                        .title("English Backend API")
                        .version("1.0")
                        .description("英语学习网站后端接口文档"))
                .components(new Components().addSecuritySchemes(schemeName, new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }

    @Bean
    public GroupedOpenApi userApiGroup() {
        return GroupedOpenApi.builder()
                .group("用户端")
                .pathsToMatch(
                        "/api/user/**",
                        "/api/plan/**",
                        "/api/practice/**",
                        "/api/test/**",
                        "/api/progress/**",
                        "/api/reward/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi adminApiGroup() {
        return GroupedOpenApi.builder()
                .group("管理端")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}