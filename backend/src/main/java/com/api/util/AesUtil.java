package com.api.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加密解密工具类
 * 用于敏感信息（如API Key、Token等）的加密存储
 */
public class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // 密钥 - 生产环境应从配置文件或环境变量读取
    private static final String SECRET_KEY = "ApiManageSystem2026SecretKey";

    /**
     * 加密
     * 
     * @param content 待加密内容
     * @return Base64编码后的加密字符串
     */
    public static String encrypt(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密
     * 
     * @param encryptedContent Base64编码的加密字符串
     * @return 解密后的原始内容
     */
    public static String decrypt(String encryptedContent) {
        if (encryptedContent == null || encryptedContent.isEmpty()) {
            return encryptedContent;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedContent);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 如果解密失败，可能是未加密的数据，直接返回
            return encryptedContent;
        }
    }

    /**
     * 脱敏显示
     * 
     * @param content 原始内容
     * @return 脱敏后的显示内容（只显示前4位和后4位）
     */
    public static String mask(String content) {
        if (content == null || content.isEmpty()) {
            return "****";
        }

        int length = content.length();
        if (length <= 8) {
            return "****" + content.substring(Math.max(0, length - 2));
        }

        return content.substring(0, 4) + "****" + content.substring(length - 4);
    }

    /**
     * 生成密钥字节数组
     */
    private static byte[] getKey() {
        try {
            // 使用固定的种子生成密钥，确保每次生成的密钥一致
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(128, random); // AES-128
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }

    /**
     * 判断字符串是否已加密（Base64格式）
     */
    public static boolean isEncrypted(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        try {
            Base64.getDecoder().decode(content);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // 测试方法
    public static void main(String[] args) {
        String original = "sk-1234567890abcdefghijklmnopqrstuvwxyz";
        System.out.println("原始内容: " + original);

        String encrypted = encrypt(original);
        System.out.println("加密后: " + encrypted);

        String decrypted = decrypt(encrypted);
        System.out.println("解密后: " + decrypted);

        String masked = mask(original);
        System.out.println("脱敏显示: " + masked);

        System.out.println("是否相同: " + original.equals(decrypted));
    }
}
