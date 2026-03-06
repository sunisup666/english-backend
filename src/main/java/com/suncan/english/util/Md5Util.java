package com.suncan.english.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 简单 MD5 工具类。
 */
public class Md5Util {
    private Md5Util() {
    }

    public static String md5(String plainText) {
        if (plainText == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean matches(String plainText, String md5Text) {
        return Objects.equals(md5(plainText), md5Text);
    }
}
