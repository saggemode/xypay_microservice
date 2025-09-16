package com.xypay.xypay.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String roles;
    private Boolean enabled;
    private LocalDateTime createdAt;
    
    // Nested DTOs
    private WalletDTO wallet;
}
