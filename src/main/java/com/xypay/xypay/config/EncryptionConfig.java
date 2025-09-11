package com.xypay.xypay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class EncryptionConfig {
    @Bean
    public TextEncryptor textEncryptor() {
        // Use strong password and salt in production!
        return Encryptors.text("demo-password", "12345678");
    }
}