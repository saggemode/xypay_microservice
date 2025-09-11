package com.xypay.xypay.service;

import com.xypay.xypay.domain.KYCProfile;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.KYCProfileRepository;
import com.xypay.xypay.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for KYC validation (BVN/NIN).
 * Equivalent to Django's BVN/NIN validation views.
 */
@Service
public class KYCValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(KYCValidationService.class);
    
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.kyc.remote-url:https://github.com/saggemode/xy_backend/blob/master/backend/dummy_kyc_data.json}")
    private String remoteKycUrl;
    
    @Value("${app.kyc.fallback-file:dummy_kyc_data.json}")
    private String fallbackFile;
    
    @Value("${app.kyc.timeout:5000}")
    private int timeoutMs;
    
    /**
     * Validate BVN and create/update KYC profile.
     * Equivalent to Django's validate_bvn view.
     */
    public KYCValidationResult validateBvn(User user, String bvn) {
        try {
            // Validate BVN format
            if (!isValidBvn(bvn)) {
                return new KYCValidationResult(false, "Invalid BVN format. Must be 11 digits.", null, false);
            }
            
            // Check if BVN already exists
            if (kycProfileRepository.existsByBvn(bvn)) {
                return new KYCValidationResult(false, "This BVN is already in use.", null, false);
            }
            
            // Load KYC data (remote with fallback)
            KYCDataResult dataResult = loadKycData();
            Map<String, Object> kycData = dataResult.getData();
            
            // Get BVN data
            Map<String, Object> bvnSection = (Map<String, Object>) kycData.get("bvn");
            if (bvnSection == null) {
                return new KYCValidationResult(false, "BVN validation service unavailable.", null, dataResult.isFallback());
            }
            
            Map<String, Object> bvnData = (Map<String, Object>) bvnSection.get(bvn);
            if (bvnData == null) {
                return new KYCValidationResult(false, "BVN not found.", null, dataResult.isFallback());
            }
            
            // Extract data
            String dobString = (String) bvnData.get("dob");
            if (dobString == null) {
                return new KYCValidationResult(false, "Date of birth is required for KYC.", null, dataResult.isFallback());
            }
            
            LocalDate dateOfBirth = LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE);
            String phone = (String) bvnData.get("phone");
            String genderStr = (String) bvnData.get("gender");
            
            // Create or update KYC profile
            Optional<KYCProfile> kycProfileOpt = kycProfileRepository.findByUser(user);
            KYCProfile kycProfile;
            if (kycProfileOpt.isPresent()) {
                kycProfile = kycProfileOpt.get();
            } else {
                kycProfile = new KYCProfile(user, dateOfBirth, ""); // Empty address initially
            }
            
            kycProfile.setBvn(bvn);
            kycProfile.setDateOfBirth(dateOfBirth);
            if (phone != null) {
                kycProfile.setTelephoneNumber(phone);
            }
            if (genderStr != null) {
                try {
                    KYCProfile.Gender gender = KYCProfile.Gender.valueOf(genderStr.toUpperCase());
                    kycProfile.setGender(gender);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid gender value: {}", genderStr);
                }
            }
            
            // Update User entity with KYC validated names
            String firstName = (String) bvnData.get("first_name");
            String lastName = (String) bvnData.get("last_name");
            if (firstName != null && lastName != null) {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                userRepository.save(user);
            }
            
            // Auto-approve for now (in production, this would go through approval process)
            kycProfile.setIsApproved(true);
            kycProfile = kycProfileRepository.save(kycProfile);
            
            // Prepare response data
            Map<String, Object> responseData = new HashMap<>(bvnData);
            responseData.put("kyc_profile_updated", true);
            
            logger.info("BVN {} validated successfully for user {}", bvn, user.getUsername());
            
            return new KYCValidationResult(true, "BVN validated successfully.", responseData, dataResult.isFallback());
            
        } catch (Exception e) {
            logger.error("Error validating BVN {} for user {}: {}", bvn, user.getUsername(), e.getMessage());
            return new KYCValidationResult(false, "BVN validation failed: " + e.getMessage(), null, false);
        }
    }
    
    /**
     * Validate NIN and create/update KYC profile.
     * Equivalent to Django's validate_nin view.
     */
    public KYCValidationResult validateNin(User user, String nin) {
        try {
            // Validate NIN format
            if (!isValidNin(nin)) {
                return new KYCValidationResult(false, "Invalid NIN format. Must be 11 digits.", null, false);
            }
            
            // Check if NIN already exists
            if (kycProfileRepository.existsByNin(nin)) {
                return new KYCValidationResult(false, "This NIN is already in use.", null, false);
            }
            
            // Load KYC data (remote with fallback)
            KYCDataResult dataResult = loadKycData();
            Map<String, Object> kycData = dataResult.getData();
            
            // Get NIN data
            Map<String, Object> ninSection = (Map<String, Object>) kycData.get("nin");
            if (ninSection == null) {
                return new KYCValidationResult(false, "NIN validation service unavailable.", null, dataResult.isFallback());
            }
            
            Map<String, Object> ninData = (Map<String, Object>) ninSection.get(nin);
            if (ninData == null) {
                return new KYCValidationResult(false, "NIN not found.", null, dataResult.isFallback());
            }
            
            // Extract data
            String dobString = (String) ninData.get("dob");
            if (dobString == null) {
                return new KYCValidationResult(false, "Date of birth is required for KYC.", null, dataResult.isFallback());
            }
            
            LocalDate dateOfBirth = LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE);
            String phone = (String) ninData.get("phone");
            String genderStr = (String) ninData.get("gender");
            
            // Create or update KYC profile
            Optional<KYCProfile> kycProfileOpt = kycProfileRepository.findByUser(user);
            KYCProfile kycProfile;
            if (kycProfileOpt.isPresent()) {
                kycProfile = kycProfileOpt.get();
            } else {
                kycProfile = new KYCProfile(user, dateOfBirth, ""); // Empty address initially
            }
            
            kycProfile.setNin(nin);
            kycProfile.setDateOfBirth(dateOfBirth);
            if (phone != null) {
                kycProfile.setTelephoneNumber(phone);
            }
            if (genderStr != null) {
                try {
                    KYCProfile.Gender gender = KYCProfile.Gender.valueOf(genderStr.toUpperCase());
                    kycProfile.setGender(gender);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid gender value: {}", genderStr);
                }
            }
            
            // Update User entity with KYC validated names
            String firstName = (String) ninData.get("first_name");
            String lastName = (String) ninData.get("last_name");
            if (firstName != null && lastName != null) {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                userRepository.save(user);
            }
            
            // Auto-approve for now (in production, this would go through approval process)
            kycProfile.setIsApproved(true);
            kycProfile = kycProfileRepository.save(kycProfile);
            
            // Prepare response data
            Map<String, Object> responseData = new HashMap<>(ninData);
            responseData.put("kyc_profile_updated", true);
            
            logger.info("NIN {} validated successfully for user {}", nin, user.getUsername());
            
            return new KYCValidationResult(true, "NIN validated successfully.", responseData, dataResult.isFallback());
            
        } catch (Exception e) {
            logger.error("Error validating NIN {} for user {}: {}", nin, user.getUsername(), e.getMessage());
            return new KYCValidationResult(false, "NIN validation failed: " + e.getMessage(), null, false);
        }
    }
    
    /**
     * Load KYC data from remote URL first, then fallback to local file.
     * Equivalent to Django's remote data fetching with fallback.
     */
    private KYCDataResult loadKycData() {
        // First try to load from remote URL with configured timeout
        try {
            logger.info("Attempting to load KYC data from remote URL: {}", remoteKycUrl);
            
            // Configure RestTemplate with timeout settings
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeoutMs); // Connection timeout
            factory.setReadTimeout(timeoutMs);    // Read timeout
            RestTemplate timeoutRestTemplate = new RestTemplate(factory);
            
            // Convert GitHub blob URL to raw content URL
            String rawUrl = remoteKycUrl.replace("github.com", "raw.githubusercontent.com")
                                      .replace("/blob", "");
            
            String jsonResponse = timeoutRestTemplate.getForObject(rawUrl, String.class);
            if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(jsonResponse, Map.class);
                logger.info("Successfully loaded KYC data from remote URL");
                return new KYCDataResult(data, false);
            }
        } catch (ResourceAccessException e) {
            logger.warn("Failed to access remote KYC data (timeout/connection after {}ms): {}", timeoutMs, e.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to load remote KYC data: {}", e.getMessage());
        }
        
        // Fallback to local file
        logger.info("Falling back to local KYC data file");
        return loadFallbackData();
    }
    
    /**
     * Load fallback KYC data from local file.
     * Equivalent to Django's load_dummy_kyc().
     */
    private KYCDataResult loadFallbackData() {
        try {
            ClassPathResource resource = new ClassPathResource(fallbackFile);
            Map<String, Object> data = objectMapper.readValue(resource.getInputStream(), Map.class);
            logger.debug("Loaded KYC data from fallback file");
            return new KYCDataResult(data, true);
            
        } catch (IOException e) {
            logger.error("Failed to load fallback KYC data: {}", e.getMessage());
            // Return empty data structure
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("bvn", new HashMap<>());
            emptyData.put("nin", new HashMap<>());
            return new KYCDataResult(emptyData, true);
        }
    }
    
    /**
     * Validate BVN format.
     */
    private boolean isValidBvn(String bvn) {
        return bvn != null && bvn.matches("\\d{11}");
    }
    
    /**
     * Validate NIN format.
     */
    private boolean isValidNin(String nin) {
        return nin != null && nin.matches("\\d{11}");
    }
    
    /**
     * Result class for KYC validation.
     */
    public static class KYCValidationResult {
        private final boolean success;
        private final String message;
        private final Map<String, Object> data;
        private final boolean fallbackUsed;
        
        public KYCValidationResult(boolean success, String message, Map<String, Object> data, boolean fallbackUsed) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.fallbackUsed = fallbackUsed;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Map<String, Object> getData() { return data; }
        public boolean isFallbackUsed() { return fallbackUsed; }
    }
    
    /**
     * Result class for KYC data loading.
     */
    private static class KYCDataResult {
        private final Map<String, Object> data;
        private final boolean fallback;
        
        public KYCDataResult(Map<String, Object> data, boolean fallback) {
            this.data = data;
            this.fallback = fallback;
        }
        
        public Map<String, Object> getData() { return data; }
        public boolean isFallback() { return fallback; }
    }
}
