package com.example.security.core.auth.application;

import com.example.security.core.auth.stub.StubOAuth2User;
import com.example.security.core.user.domain.entity.OAuth2UserInfo;
import com.example.security.core.user.domain.repository.OAuth2UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2UserService 단위 테스트")
public class CustomOAuth2UserServiceTest {
    @Mock
    private OAuth2UserInfoRepository userInfoRepository;
    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    private CustomOAuth2UserService service;

    @BeforeEach
    void setup() {
        service = new CustomOAuth2UserService(userInfoRepository, defaultOAuth2UserService);
    }

    @Test
    @DisplayName("지원하지 않는 registrationId로 요청 시 OAuth2AuthenticationException 발생")
    void loadUser_withUnsupportedRegistrationId_shouldThrowException() {
        // given
        OAuth2UserRequest googleRequest = StubOAuth2User.create("google");

        OAuth2User stubUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("id", "stub"),
                "id"
        );

        // registrationId == "google" 인 경우에만 delegate mock 작동
        given(defaultOAuth2UserService.loadUser(
                argThat(req -> "google".equals(req.getClientRegistration().getRegistrationId()))
        )).willReturn(stubUser);

        // when / then
        assertThatThrownBy(() -> service.loadUser(googleRequest))
                .isInstanceOf(OAuth2AuthenticationException.class);

    }

    @Test
    @DisplayName("Kakao 응답 구조가 비정상일 경우 NullPointerException 발생")
    void loadUser_withInvalidKakaoAttributes_shouldThrowNPE() {
        // given
        OAuth2UserRequest kakaoRequest = StubOAuth2User.create("kakao");

        // kakao_account 누락된 비정상 응답 구조
        OAuth2User invalidKakaoUser = StubOAuth2User.invalidKakaoUser();

        // delegate(mock)가 해당 응답 반환하도록 stub
        given(defaultOAuth2UserService.loadUser(
                argThat(req -> "kakao".equals(req.getClientRegistration().getRegistrationId()))
        )).willReturn(invalidKakaoUser);

        // when / then
        assertThatThrownBy(() -> service.loadUser(kakaoRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Repository 저장 중 예외 발생 시 예외 전파 확인")
    void loadUser_repositorySaveThrowsException_shouldPropagate() {
        // given
        OAuth2UserRequest kakaoRequest = StubOAuth2User.create("kakao");

        OAuth2User kakaoUser = StubOAuth2User.kakaoUser();

        // delegate mock이 정상적인 kakao 응답 반환
        given(defaultOAuth2UserService.loadUser(
                argThat(req -> "kakao".equals(req.getClientRegistration().getRegistrationId()))
        )).willReturn(kakaoUser);

        // Repository save 호출 시 RuntimeException 발생
        willThrow(new RuntimeException("DB Error"))
                .given(userInfoRepository)
                .save(any(OAuth2UserInfo.class));

        // when / then
        assertThatThrownBy(() -> service.loadUser(kakaoRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB Error");
    }
}
