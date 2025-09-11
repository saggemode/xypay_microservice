package com.xypay.analytics.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}
