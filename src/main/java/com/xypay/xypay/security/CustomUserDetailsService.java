package com.xypay.xypay.security;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            // Try to find by email as well
            userOpt = userRepository.findByEmail(username);
        }
        
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        User user = userOpt.get();
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add default user role
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Add roles from user's roles field (comma-separated string)
        if (user.getRoles() != null && !user.getRoles().trim().isEmpty()) {
            String[] roles = user.getRoles().split(",");
            for (String role : roles) {
                String trimmedRole = role.trim();
                if (!trimmedRole.isEmpty()) {
                    // Ensure role starts with ROLE_ prefix
                    if (!trimmedRole.startsWith("ROLE_")) {
                        trimmedRole = "ROLE_" + trimmedRole;
                    }
                    authorities.add(new SimpleGrantedAuthority(trimmedRole));
                }
            }
        }
        
        return authorities;
    }
}
