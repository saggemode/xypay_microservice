package com.xypay.customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class EncryptionConfig {
    
    @Value("${app.encryption.key:default-encryption-key-change-in-production}")
    private String encryptionKey;
    
    @Value("${app.encryption.salt:default-salt-change-in-production}")
    private String salt;
    
    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.text(encryptionKey, salt);
    }
    
    @Bean
    public PIIEncryptionService piiEncryptionService() {
        return new PIIEncryptionService(encryptionKey);
    }
    
    public static class PIIEncryptionService {
        private static final String ALGORITHM = "AES";
        private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
        private final SecretKey secretKey;
        
        public PIIEncryptionService(String keyString) {
            // Generate a consistent key from the string
            byte[] key = keyString.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = new byte[16]; // AES-128
            System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, 16));
            this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        }
        
        public String encrypt(String plainText) {
            if (plainText == null || plainText.isEmpty()) {
                return plainText;
            }
            
            try {
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt data", e);
            }
        }
        
        public String decrypt(String encryptedText) {
            if (encryptedText == null || encryptedText.isEmpty()) {
                return encryptedText;
            }
            
            try {
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
                byte[] decryptedBytes = cipher.doFinal(decodedBytes);
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrypt data", e);
            }
        }
        
        public String encryptEmail(String email) {
            return encrypt(email);
        }
        
        public String decryptEmail(String encryptedEmail) {
            return decrypt(encryptedEmail);
        }
        
        public String encryptPhone(String phone) {
            return encrypt(phone);
        }
        
        public String decryptPhone(String encryptedPhone) {
            return decrypt(encryptedPhone);
        }
        
        public String encryptAddress(String address) {
            return encrypt(address);
        }
        
        public String decryptAddress(String encryptedAddress) {
            return decrypt(encryptedAddress);
        }
        
        public String encryptBVN(String bvn) {
            return encrypt(bvn);
        }
        
        public String decryptBVN(String encryptedBVN) {
            return decrypt(encryptedBVN);
        }
        
        public String encryptNIN(String nin) {
            return encrypt(nin);
        }
        
        public String decryptNIN(String encryptedNIN) {
            return decrypt(encryptedNIN);
        }
        
        public String encryptName(String name) {
            return encrypt(name);
        }
        
        public String decryptName(String encryptedName) {
            return decrypt(encryptedName);
        }
    }
}
