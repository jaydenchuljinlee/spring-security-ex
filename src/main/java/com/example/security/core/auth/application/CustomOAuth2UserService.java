package com.example.security.core.auth.application;

import com.example.security.core.auth.dto.CustomOAuth2User;
import com.example.security.core.user.domain.entity.OAuth2UserInfo;
import com.example.security.core.user.domain.entity.enums.UserRole;
import com.example.security.core.user.domain.repository.OAuth2UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserInfoRepository oauth2UserInfoRepository;
    private final DefaultOAuth2UserService delegate;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo user = processOAuth2User(registrationId, oAuth2User.getAttributes());

        return new CustomOAuth2User(
                user.getId(),
                user.getName(),
                user.getRole(),
                oAuth2User.getAttributes()
        );
    }

    private OAuth2UserInfo processOAuth2User(String registrationId, Map<String, Object> attributes) {
        String providerId;
        String email;
        String name;

        if (registrationId.equals("kakao")) {
            providerId = String.valueOf(attributes.get("id"));

            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        } else {
            // 다른 OAuth2 제공자(Google, Naver 등)를 위한 처리 로직
            throw new OAuth2AuthenticationException("Unsupported registrationId: " + registrationId);
        }

        Optional<OAuth2UserInfo> userOptional = oauth2UserInfoRepository.findByProviderAndProviderId(registrationId, providerId);

        OAuth2UserInfo user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 필요하다면 여기서 사용자 정보(이름, 이메일 등)를 업데이트할 수 있습니다.
        } else {
            user = OAuth2UserInfo.builder()
                    .provider(registrationId)
                    .providerId(providerId)
                    .name(name)
                    .email(email)
                    .role(UserRole.USER)
                    .build();
            oauth2UserInfoRepository.save(user);
        }

        return user;
    }
}
