package com.suncan.english;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类。
 */
@SpringBootApplication
@MapperScan("com.suncan.english.mapper")
public class EnglishApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnglishApplication.class, args);
    }
}
