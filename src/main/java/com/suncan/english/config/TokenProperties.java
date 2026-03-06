package com.suncan.english.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * token 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    /**
     * 签名密钥，建议至少 32 位。
     */
    private String secret;

    /**
     * token 过期时间，单位小时。
     */
    private Integer expireHours = 24;

    /**
     * 请求头名称，默认 Authorization。
     */
    private String header = "Authorization";
}
