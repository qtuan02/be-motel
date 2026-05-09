package com.motel.user_service.adapter;

public interface PiiCryptoService {
    String encrypt(String rawValue);

    String decrypt(String encryptedValue);

    String hash(String rawValue);

    String maskPhone(String rawPhone);
}
