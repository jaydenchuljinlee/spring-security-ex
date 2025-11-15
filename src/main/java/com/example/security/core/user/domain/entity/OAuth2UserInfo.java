package com.example.security.core.user.domain.entity;

import com.example.security.core.user.domain.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "oauth2_user_info")
public class OAuth2UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider; // "kakao", "google" 등
    private String providerId; // 제공자별 고유 ID

    private String email;
    private String name;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public OAuth2UserInfo(String provider, String providerId, String email, String name, UserRole role) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
