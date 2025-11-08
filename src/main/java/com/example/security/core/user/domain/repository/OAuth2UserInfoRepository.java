package com.example.security.core.user.domain.repository;

import com.example.security.core.user.domain.entity.OAuth2UserInfo;

import java.util.Optional;

public interface OAuth2UserInfoRepository {
    Optional<OAuth2UserInfo> findByProviderAndProviderId(String provider, String providerId);

    OAuth2UserInfo save(OAuth2UserInfo entity);
}
