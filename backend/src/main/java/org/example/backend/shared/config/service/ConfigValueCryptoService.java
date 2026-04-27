package org.example.backend.shared.config.service;

import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AI 索引: 系统敏感配置 AES 加解密服务。
 */
@Service
public class ConfigValueCryptoService {

    private static final String PREFIX = "v1:";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final String configSecret;
    private final SecureRandom secureRandom = new SecureRandom();

    public ConfigValueCryptoService(@Value("${llm.config-secret:}") String configSecret) {
        this.configSecret = configSecret;
    }

    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return "";
        }
        requireSecret();
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(deriveKey(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new BusinessException(ApiResponseCode.INTERNAL_ERROR, "敏感配置加密失败");
        }
    }

    public String decrypt(String storedValue) {
        if (!StringUtils.hasText(storedValue)) {
            return "";
        }
        if (!storedValue.startsWith(PREFIX)) {
            return storedValue;
        }
        requireSecret();
        try {
            String[] parts = storedValue.substring(PREFIX.length()).split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted config value");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(deriveKey(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ApiResponseCode.INTERNAL_ERROR, "敏感配置解密失败，请检查部署密钥");
        }
    }

    private byte[] deriveKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(configSecret.getBytes(StandardCharsets.UTF_8));
    }

    private void requireSecret() {
        if (!StringUtils.hasText(configSecret)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "未配置 LLM_CONFIG_SECRET，不能保存或读取敏感配置");
        }
    }
}
