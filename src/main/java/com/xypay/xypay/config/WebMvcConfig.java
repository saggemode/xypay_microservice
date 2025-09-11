package com.xypay.xypay.config;

import com.xypay.xypay.interceptor.SecurityInterceptor;
import com.xypay.xypay.interceptor.LoginSecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private SecurityInterceptor securityInterceptor;
    
    @Autowired
    private LoginSecurityInterceptor loginSecurityInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor);
        registry.addInterceptor(loginSecurityInterceptor);
    }
}