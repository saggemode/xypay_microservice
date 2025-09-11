package com.xypay.customer.domain;

import com.xypay.customer.config.EncryptionConfig;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@Table(name = "encrypted_users")
@Component
public class EncryptedUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Encrypted PII fields
    @Column(name = "encrypted_first_name")
    private String encryptedFirstName;

    @Column(name = "encrypted_last_name")
    private String encryptedLastName;

    @Column(name = "encrypted_email")
    private String encryptedEmail;

    private String roles; // e.g., "ROLE_ADMIN"
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Transient fields for decrypted data (not persisted)
    @Transient
    private String firstName;

    @Transient
    private String lastName;

    @Transient
    private String email;

    // Inject encryption service
    @Transient
    @Autowired
    private EncryptionConfig.PIIEncryptionService encryptionService;

    // Constructors
    public EncryptedUser() {}

    public EncryptedUser(String username, String password, String firstName, String lastName, String email, String roles) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.enabled = true;
        encryptPII();
    }

    // Encryption/Decryption methods
    public void encryptPII() {
        if (encryptionService != null) {
            if (firstName != null) {
                this.encryptedFirstName = encryptionService.encryptName(firstName);
            }
            if (lastName != null) {
                this.encryptedLastName = encryptionService.encryptName(lastName);
            }
            if (email != null) {
                this.encryptedEmail = encryptionService.encryptEmail(email);
            }
        }
    }

    public void decryptPII() {
        if (encryptionService != null) {
            if (encryptedFirstName != null) {
                this.firstName = encryptionService.decryptName(encryptedFirstName);
            }
            if (encryptedLastName != null) {
                this.lastName = encryptionService.decryptName(encryptedLastName);
            }
            if (encryptedEmail != null) {
                this.email = encryptionService.decryptEmail(encryptedEmail);
            }
        }
    }

    // Business methods
    public void updatePersonalInfo(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        encryptPII();
    }

    public String getFullName() {
        decryptPII();
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getDisplayName() {
        decryptPII();
        return firstName != null ? firstName : username;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Encrypted field getters/setters
    public String getEncryptedFirstName() { return encryptedFirstName; }
    public void setEncryptedFirstName(String encryptedFirstName) { this.encryptedFirstName = encryptedFirstName; }

    public String getEncryptedLastName() { return encryptedLastName; }
    public void setEncryptedLastName(String encryptedLastName) { this.encryptedLastName = encryptedLastName; }

    public String getEncryptedEmail() { return encryptedEmail; }
    public void setEncryptedEmail(String encryptedEmail) { this.encryptedEmail = encryptedEmail; }

    // Transient field getters/setters (these will decrypt on access)
    public String getFirstName() { 
        if (firstName == null && encryptedFirstName != null) {
            decryptPII();
        }
        return firstName; 
    }
    public void setFirstName(String firstName) { 
        this.firstName = firstName;
        if (encryptionService != null) {
            this.encryptedFirstName = encryptionService.encryptName(firstName);
        }
    }

    public String getLastName() { 
        if (lastName == null && encryptedLastName != null) {
            decryptPII();
        }
        return lastName; 
    }
    public void setLastName(String lastName) { 
        this.lastName = lastName;
        if (encryptionService != null) {
            this.encryptedLastName = encryptionService.encryptName(lastName);
        }
    }

    public String getEmail() { 
        if (email == null && encryptedEmail != null) {
            decryptPII();
        }
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email;
        if (encryptionService != null) {
            this.encryptedEmail = encryptionService.encryptEmail(email);
        }
    }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Utility methods for conversion
    public User toUser() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setFirstName(this.getFirstName());
        user.setLastName(this.getLastName());
        user.setEmail(this.getEmail());
        user.setRoles(this.roles);
        user.setEnabled(this.enabled);
        user.setCreatedAt(this.createdAt);
        return user;
    }

    public static EncryptedUser fromUser(User user) {
        EncryptedUser encryptedUser = new EncryptedUser();
        encryptedUser.setId(user.getId());
        encryptedUser.setUsername(user.getUsername());
        encryptedUser.setPassword(user.getPassword());
        encryptedUser.setFirstName(user.getFirstName());
        encryptedUser.setLastName(user.getLastName());
        encryptedUser.setEmail(user.getEmail());
        encryptedUser.setRoles(user.getRoles());
        encryptedUser.setEnabled(user.isEnabled());
        encryptedUser.setCreatedAt(user.getCreatedAt());
        return encryptedUser;
    }
}
