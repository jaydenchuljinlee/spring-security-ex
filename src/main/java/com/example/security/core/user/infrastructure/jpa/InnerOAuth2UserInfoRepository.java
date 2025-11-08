package com.example.security.core.user.infrastructure.jpa;

import com.example.security.core.user.domain.entity.OAuth2UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InnerOAuth2UserInfoRepository extends JpaRepository<OAuth2UserInfo, Long> {
    Optional<OAuth2UserInfo> findByProviderAndProviderId(String provider, String providerId);
}
