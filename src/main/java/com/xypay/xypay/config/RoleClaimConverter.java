package com.xypay.xypay.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
/*
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
*/

import java.util.Collection;
import java.util.Collections;

// Commenting out the implementation since OAuth2 dependencies are not available
// This class would normally convert JWT claims to authorities
public class RoleClaimConverter implements Converter<Object, AbstractAuthenticationToken> {
    
    @Override
    public AbstractAuthenticationToken convert(Object source) {
        // Return a basic authentication token without authorities
        return null;
        /*
        Collection<GrantedAuthority> authorities = Collections.emptyList();
        
        if (jwt.getClaims().containsKey("scope")) {
            authorities = jwt.getClaimAsStringList("scope").stream()
                    .map(authority -> "SCOPE_" + authority)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        
        return new JwtAuthenticationToken(jwt, authorities);
        */
    }
}