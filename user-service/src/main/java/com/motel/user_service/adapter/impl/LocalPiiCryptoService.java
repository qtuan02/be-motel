package com.motel.user_service.adapter.impl;

import com.motel.user_service.adapter.PiiCryptoService;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalPiiCryptoService implements PiiCryptoService {
    private static final Logger log = LoggerFactory.getLogger(LocalPiiCryptoService.class);
    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKey encryptionKey;
    private final SecureRandom secureRandom;

    public LocalPiiCryptoService(@Value("${app.pii.encryption-key-base64:}") String configuredKeyBase64) {
        this.secureRandom = new SecureRandom();
        this.encryptionKey = resolveKey(configuredKeyBase64);
    }

    @Override
    public String encrypt(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(cipherText, 0, payload, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to encrypt sensitive value", ex);
        }
    }

    @Override
    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return null;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedValue);
            if (payload.length <= IV_LENGTH_BYTES) {
                throw new IllegalStateException("Invalid encrypted payload");
            }
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] cipherText = new byte[payload.length - IV_LENGTH_BYTES];
            System.arraycopy(payload, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(payload, IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Failed to decrypt sensitive value", ex);
        }
    }

    @Override
    public String hash(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawValue.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    @Override
    public String maskPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return null;
        }
        String cleaned = rawPhone.replaceAll("\\s+", "");
        if (cleaned.length() <= 4) {
            return "****";
        }
        String suffix = cleaned.substring(cleaned.length() - 4);
        return "***" + suffix;
    }

    private SecretKey resolveKey(String configuredKeyBase64) {
        if (configuredKeyBase64 != null && !configuredKeyBase64.isBlank()) {
            byte[] decodedKey = Base64.getDecoder().decode(configuredKeyBase64.trim());
            validateKeyLength(decodedKey.length);
            return new SecretKeySpec(decodedKey, AES);
        }

        byte[] fallbackKey = new byte[KEY_LENGTH_BYTES];
        secureRandom.nextBytes(fallbackKey);
        log.warn(
                "APP_PII_ENCRYPTION_KEY_BASE64 is not configured. Generated ephemeral encryption key for current process.");
        return new SecretKeySpec(fallbackKey, AES);
    }

    private void validateKeyLength(int keyLengthBytes) {
        if (keyLengthBytes != 16 && keyLengthBytes != 24 && keyLengthBytes != 32) {
            throw new IllegalStateException("Invalid AES key length. Supported lengths are 16, 24, or 32 bytes.");
        }
    }
}
