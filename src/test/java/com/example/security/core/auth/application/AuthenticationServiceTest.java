package com.example.security.core.auth.application;

import com.example.security.core.auth.domain.exceptions.AuthenticationFailException;
import com.example.security.core.user.domain.entity.User;
import com.example.security.core.user.domain.repository.UserDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService 단위 테스트")
public class AuthenticationServiceTest {
    @Mock
    private UserDetailRepository userDetailRepository;

    private PasswordEncoder passwordEncoder;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        // 실제 Argon2PasswordEncoder 사용 (mock 아님)
        passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        authenticationService = new AuthenticationService(userDetailRepository, passwordEncoder);
    }

    @Test
    @DisplayName("가입되지 않은 사용자는 AuthenticationFailException.userNotRegistered 예외 발생")
    void authenticate_userNotRegistered_shouldThrowException() {
        // given
        String email = "notfound@example.com";
        String password = "password123";

        given(userDetailRepository.findByEmail(email)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authenticationService.authenticate(email, password))
                .isInstanceOf(AuthenticationFailException.class)
                .hasMessageContaining("Authentication failed - User not registered with the email: " + email);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 AuthenticationFailException.passwordNotMatched 예외 발생")
    void authenticate_passwordNotMatched_shouldThrowException() {
        // given
        String email = "iron@example.com";
        String name = "ironjin";
        String rawPassword = "wrong_password";
        String phoneNumber = "01012345678";

        String encodedPassword = passwordEncoder.encode("correct_password");
        User mockUser = User.builder()
                .email(email)
                .userName(name)
                .encodedPassword(encodedPassword)
                .phoneNumber(phoneNumber)
                .build();

        given(userDetailRepository.findByEmail(email)).willReturn(Optional.of(mockUser));

        // when / then
        assertThatThrownBy(() -> authenticationService.authenticate(email, rawPassword))
                .isInstanceOf(AuthenticationFailException.class)
                .hasMessageContaining("Authentication failed - Password is not matched with the email:" + email);
    }

    @Test
    @DisplayName("정상적인 이메일과 비밀번호로 인증 시 User 반환")
    void authenticate_success_shouldReturnUser() {
        // given
        String email = "iron@example.com";
        String name = "ironjin";
        String rawPassword = "correct_password";
        String phoneNumber = "01012345678";

        String encodedPassword = passwordEncoder.encode("correct_password");
        User mockUser = User.builder()
                .email(email)
                .userName(name)
                .encodedPassword(encodedPassword)
                .phoneNumber(phoneNumber)
                .build();

        given(userDetailRepository.findByEmail(email)).willReturn(Optional.of(mockUser));

        // when
        User result = authenticationService.authenticate(email, rawPassword);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

}
