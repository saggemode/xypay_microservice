package com.xypay.xypay.config;

import com.xypay.xypay.security.CustomUserDetailsService;
import com.xypay.xypay.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/setup", "/error", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/teller/**").hasRole("TELLER")
                .requestMatchers("/cso/**").hasRole("CUSTOMER_SERVICE_OFFICER")
                .requestMatchers("/loan-officer/**").hasRole("LOAN_OFFICER")
                .requestMatchers("/rm/**").hasRole("RELATIONSHIP_MANAGER")
                .requestMatchers("/admin/cbn-levies/**").permitAll()
                .requestMatchers("/admin/xysave/api/**").hasAnyRole("SUPERUSER", "ADMIN")
                .requestMatchers("/admin/api/**").hasAnyRole("SUPERUSER", "ADMIN")
                .requestMatchers("/admin/**").hasAnyRole("SUPERUSER", "ADMIN")
                .requestMatchers("/spend-save/**").hasAnyRole("USER", "ADMIN", "SUPERUSER")
                .requestMatchers("/api/transactions/**").hasAuthority("SCOPE_transaction")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/admin/xysave/api/**")
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry())
            )
            .userDetailsService(userDetailsService)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String targetUrl = "/user/dashboard";

            if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_SUPERUSER".equals(a.getAuthority()))) {
                targetUrl = "/superuser/dashboard/legacy";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                targetUrl = "/admin/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TELLER".equals(a.getAuthority()))) {
                targetUrl = "/teller/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_CUSTOMER_SERVICE_OFFICER".equals(a.getAuthority()))) {
                targetUrl = "/cso/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_LOAN_OFFICER".equals(a.getAuthority()))) {
                targetUrl = "/loan-officer/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_RELATIONSHIP_MANAGER".equals(a.getAuthority()))) {
                targetUrl = "/rm/dashboard";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority()))) {
                targetUrl = "/user/dashboard";
            }

            response.sendRedirect(targetUrl);
        };
    }
}