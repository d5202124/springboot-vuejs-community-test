package com.example.esun.common;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Component
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536; 
    private static final int KEY_LENGTH = 256;   
    private final SecureRandom secureRandom = new SecureRandom();

    // 加鹽雜湊，回傳 "salt:hash" 格式字串存入資料庫
    public String hashPassword(String password) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt); 
        byte[] hash = pbkdf2(password.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    
    public boolean verifyPassword(String password, String stored) {
        String[] parts = stored.split(":");
        if (parts.length != 2) return false;

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = pbkdf2(password.toCharArray(), salt);

        if (expectedHash.length != actualHash.length) return false;

        int result = 0;
        for (int i = 0; i < expectedHash.length; i++) {
            result |= expectedHash[i] ^ actualHash[i];
        }
        return result == 0;
    }

    // 雜湊計算
    private byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("無法建立密碼雜湊", e);
        }
    }
}
