package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SetupService implements SetupServiceInterface {
    
    @Autowired
    private DatabaseSetupService databaseSetupService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Check if the system has been initialized
     * @return true if system is initialized, false otherwise
     */
    @Override
    public boolean isSystemInitialized() {
        return databaseSetupService.isSystemInitialized();
    }
    
    /**
     * Initialize the system with default configurations
     * @param bankName The name of the bank
     * @param bankCode The code for the main branch
     * @param adminUser The admin user details
     * @return true if successful, false otherwise
     */
    @Override
    public boolean initializeSystem(String bankName, String bankCode, AdminUser adminUser) {
        // Validate inputs
        if (bankName == null || bankName.isEmpty()) {
            return false;
        }
        
        if (bankCode == null || bankCode.isEmpty()) {
            return false;
        }
        
        if (adminUser == null || !adminUser.isValid()) {
            return false;
        }
        // Check if user already exists
        if (userRepository.findByUsername(adminUser.getUsername()).isPresent()) {
            return false;
        }
        // Create and save admin user
        User user = new User();
        user.setUsername(adminUser.getUsername());
        user.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        user.setFirstName(adminUser.getFirstName());
        user.setLastName(adminUser.getLastName());
        user.setEmail(adminUser.getEmail());
        user.setEnabled(true);
        // Assign SUPERUSER role if this is the first user
        if (userRepository.count() == 0) {
            user.setRoles("ROLE_SUPERUSER,ROLE_ADMIN");
        } else {
            user.setRoles("ROLE_ADMIN");
        }
        userRepository.save(user);
        return databaseSetupService.initializeSystem(bankName, bankCode, adminUser);
    }
    
    /**
     * Inner class to represent admin user details
     */
    public static class AdminUser {
        private String username;
        private String password;
        private String firstName;
        private String lastName;
        private String email;
        
        // Constructors
        public AdminUser() {}
        
        public AdminUser(String username, String password, String firstName, String lastName, String email) {
            this.username = username;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        /**
         * Validate the admin user details
         * @return true if valid, false otherwise
         */
        public boolean isValid() {
            return username != null && !username.isEmpty() &&
                   password != null && password.length() >= 6 &&
                   firstName != null && !firstName.isEmpty() &&
                   lastName != null && !lastName.isEmpty() &&
                   email != null && !email.isEmpty() && email.contains("@");
        }
    }
}