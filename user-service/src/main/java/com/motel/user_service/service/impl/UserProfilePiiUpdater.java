package com.motel.user_service.service.impl;

import com.motel.user_service.adapter.PiiCryptoService;
import com.motel.user_service.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfilePiiUpdater {
    private final PiiCryptoService piiCryptoService;

    public UserProfilePiiUpdater(PiiCryptoService piiCryptoService) {
        this.piiCryptoService = piiCryptoService;
    }

    public void applyPhone(UserProfile profile, String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            profile.setPhoneCiphertext(null);
            profile.setPhoneHash(null);
            profile.setPhoneMasked(null);
            return;
        }
        String normalizedPhone = rawPhone.trim();
        profile.setPhoneCiphertext(piiCryptoService.encrypt(normalizedPhone));
        profile.setPhoneHash(piiCryptoService.hash(normalizedPhone));
        profile.setPhoneMasked(piiCryptoService.maskPhone(normalizedPhone));
    }

    public void applyZaloUid(UserProfile profile, String rawZaloUid) {
        if (rawZaloUid == null || rawZaloUid.isBlank()) {
            profile.setZaloUidCiphertext(null);
            profile.setZaloUidHash(null);
            return;
        }
        String normalizedZaloUid = rawZaloUid.trim();
        profile.setZaloUidCiphertext(piiCryptoService.encrypt(normalizedZaloUid));
        profile.setZaloUidHash(piiCryptoService.hash(normalizedZaloUid));
    }
}
