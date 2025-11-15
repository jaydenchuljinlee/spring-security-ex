package com.example.security.core.user.domain.entity;

import com.example.security.core.user.domain.entity.enums.Gender;
import com.example.security.core.user.domain.entity.enums.UserStatus;
import com.example.security.core.user.domain.exceptions.UserDomainValueException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("User 엔티티 단위 테스트")
public class UserTest {
    @Test
    @DisplayName("올바른 정보로 생성 시 기본 상태는 NORMAL, 로그인 실패 횟수 0")
    void createUser_success() {
        User user = User.builder()
                .email("test@example.com")
                .encodedPassword("encoded_pw")
                .userName("홍길동")
                .phoneNumber("01012345678")
                .gender(Gender.MALE)
                .build();

        assertThat(user.getUserStatus()).isEqualTo(UserStatus.NORMAL);
        assertThat(user.getLoginFailCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("잘못된 이메일 형식이면 예외 발생")
    void invalidEmail_shouldThrowException() {
        assertThatThrownBy(() -> User.builder()
                .email("invalid-email")
                .encodedPassword("encoded_pw")
                .userName("홍길동")
                .phoneNumber("01012345678")
                .gender(Gender.MALE)
                .build())
                .isInstanceOf(UserDomainValueException.class)
                .hasMessageContaining("User.email");
    }

    @Test
    @DisplayName("비밀번호가 비어 있으면 예외 발생")
    void blankPassword_shouldThrowException() {
        assertThatThrownBy(() -> User.builder()
                .email("test@example.com")
                .encodedPassword(" ")
                .userName("홍길동")
                .phoneNumber("01012345678")
                .gender(Gender.MALE)
                .build())
                .isInstanceOf(UserDomainValueException.class)
                .hasMessageContaining("User.encodedPassword");
    }

    @Test
    @DisplayName("잘못된 이름 형식이면 예외 발생")
    void invalidName_shouldThrowException() {
        assertThatThrownBy(() -> User.builder()
                .email("test@example.com")
                .encodedPassword("encoded_pw")
                .userName("@@@")
                .phoneNumber("01012345678")
                .gender(Gender.MALE)
                .build())
                .isInstanceOf(UserDomainValueException.class)
                .hasMessageContaining("User.userName");
    }

    @Test
    @DisplayName("잘못된 전화번호 형식이면 예외 발생")
    void invalidPhone_shouldThrowException() {
        assertThatThrownBy(() -> User.builder()
                .email("test@example.com")
                .encodedPassword("encoded_pw")
                .userName("홍길동")
                .phoneNumber("1234")
                .gender(Gender.MALE)
                .build())
                .isInstanceOf(UserDomainValueException.class)
                .hasMessageContaining("User.phoneNumber");
    }

    @Test
    @DisplayName("UserStatus에 따른 ROLE 반환 확인")
    void authoritiesByStatus_shouldReturnProperRole() {
        User user = User.builder()
                .email("test@example.com")
                .encodedPassword("encoded_pw")
                .userName("홍길동")
                .phoneNumber("01012345678")
                .gender(Gender.MALE)
                .build();

        boolean hasMember = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MEMBER"::equals);
        assertTrue(hasMember, "NORMAL이면 ROLE_MEMBER 권한이 있어야 합니다.");


        user.assignStatus(UserStatus.ASK);
        boolean hasAsk = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ASK"::equals);
        assertTrue(hasAsk, "ASK이면 ROLE_ASK 권한이 있어야 합니다.");


        user.assignStatus(UserStatus.DROPOUT);
        boolean hasDropout = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DROPOUT"::equals);
        assertTrue(hasDropout, "DROPOUT이면 ROLE_DROPOUT 권한이 있어야 합니다.");
    }

}
