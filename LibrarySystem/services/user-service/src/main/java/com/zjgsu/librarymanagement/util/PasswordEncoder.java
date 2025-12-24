package com.zjgsu.librarymanagement.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncoder {

    // 使用SHA-256加盐加密
    public static String encode(String rawPassword) {
        try {
            // 生成随机盐
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // 创建消息摘要
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(rawPassword.getBytes());

            // 组合盐和哈希密码
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    // 验证密码
    public static boolean matches(String rawPassword, String encodedPassword) {
        try {
            byte[] combined = Base64.getDecoder().decode(encodedPassword);

            // 提取盐（前16字节）
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, salt.length);

            // 计算输入密码的哈希
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedInput = md.digest(rawPassword.getBytes());

            // 提取存储的哈希（盐后面的部分）
            byte[] storedHash = new byte[combined.length - salt.length];
            System.arraycopy(combined, salt.length, storedHash, 0, storedHash.length);

            // 比较哈希值
            return MessageDigest.isEqual(hashedInput, storedHash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码验证失败", e);
        }
    }
}