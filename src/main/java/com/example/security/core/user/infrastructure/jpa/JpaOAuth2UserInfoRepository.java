package com.example.security.core.user.infrastructure.jpa;

import com.example.security.core.user.domain.entity.OAuth2UserInfo;
import com.example.security.core.user.domain.repository.OAuth2UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaOAuth2UserInfoRepository implements OAuth2UserInfoRepository {
    private final InnerOAuth2UserInfoRepository repository;

    @Override
    public Optional<OAuth2UserInfo> findByProviderAndProviderId(String provider, String providerId) {
        return repository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public OAuth2UserInfo save(OAuth2UserInfo entity) {
        return repository.save(entity);
    }
}
