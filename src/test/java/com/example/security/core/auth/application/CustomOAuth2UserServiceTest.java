package com.example.security.core.auth.application;

import com.example.security.core.auth.stub.StubOAuth2User;
import com.example.security.core.user.domain.repository.OAuth2UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;

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

//    @Test
//    @DisplayName("Kakao 응답 구조가 비정상일 경우 NullPointerException 발생")
//    void loadUser_withInvalidKakaoAttributes_shouldThrowNPE() {
//        // given
//        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
//        OAuth2User mockUser = mock(OAuth2User.class);
//
//        given(userRequest.getClientRegistration().getRegistrationId()).willReturn("kakao");
//        // kakao_account 누락
//        given(mockUser.getAttributes()).willReturn(Map.of("id", "12345"));
//
//        doReturn(mockUser).when(service).loadUser(userRequest);
//
//        // when / then
//        assertThatThrownBy(() -> service.loadUser(userRequest))
//                .isInstanceOf(NullPointerException.class);
//    }
//
//    @Test
//    @DisplayName("Repository 저장 중 예외 발생 시 예외 전파 확인")
//    void loadUser_repositorySaveThrowsException_shouldPropagate() {
//        // given
//        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
//        OAuth2User mockUser = mock(OAuth2User.class);
//
//        given(userRequest.getClientRegistration().getRegistrationId()).willReturn("kakao");
//
//        Map<String, Object> profile = Map.of("nickname", "Iron");
//        Map<String, Object> kakaoAccount = Map.of(
//                "email", "ironjin@kakao.com",
//                "profile", profile
//        );
//        Map<String, Object> attributes = Map.of(
//                "id", "9999",
//                "kakao_account", kakaoAccount
//        );
//
//        given(mockUser.getAttributes()).willReturn(attributes);
//        doReturn(mockUser).when((DefaultOAuth2UserService) customOAuth2UserService).loadUser(userRequest);
//
//        willThrow(new RuntimeException("DB Error"))
//                .given(userInfoRepository).save(any(OAuth2UserInfo.class));
//
//        // when / then
//        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("DB Error");
//    }
}
