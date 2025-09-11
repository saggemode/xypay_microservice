package com.xypay.xypay.dto;

import java.util.UUID;

public class FixedSavingsPayoutDTO {
    private UUID fixedSavingsId;

    // Getters and setters
    public UUID getFixedSavingsId() {
        return fixedSavingsId;
    }

    public void setFixedSavingsId(UUID fixedSavingsId) {
        this.fixedSavingsId = fixedSavingsId;
    }
}