package com.xypay.xypay.service;

public interface SetupServiceInterface {
    boolean isSystemInitialized();
    boolean initializeSystem(String bankName, String bankCode, SetupService.AdminUser adminUser);
}