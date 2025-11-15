package com.example.security.core.auth.dto;


import com.example.security.core.user.domain.entity.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long id;
    private final String name;
    private final UserRole role;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Long id, String name, UserRole role, Map<String, Object> attributes) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getKey()));
    }

    @Override
    public String getName() {
        // OAuth2 에서는 보통 providerId를 name으로 사용합니다.
        return String.valueOf(id);
    }
}
