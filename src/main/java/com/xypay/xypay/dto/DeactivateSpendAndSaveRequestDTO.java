package com.xypay.xypay.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class DeactivateSpendAndSaveRequestDTO {
    
    @NotNull(message = "Confirmation is required")
    @AssertTrue(message = "You must confirm deactivation")
    private Boolean confirmDeactivation;
}
