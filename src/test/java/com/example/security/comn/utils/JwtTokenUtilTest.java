package com.example.security.comn.utils;

import com.example.security.comn.properties.JwtProperties;
import com.example.security.core.auth.domain.exceptions.InvalidTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("JwtTokenUtil의 테스트")
public class JwtTokenUtilTest {
    private final String EMAIL = "ironjin92@gmail.com";

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setup() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("youcantrevealthesecretkey1234012300040");
        jwtProperties.setTokenExpirationHour(1480849147370L);
        jwtTokenUtil = new JwtTokenUtil(jwtProperties);
    }

    @ParameterizedTest(name = "Authorization 헤더가 [{0}]이면 null을 반환한다")
    @NullAndEmptySource
    @ValueSource(strings = {"InvalidHeader something", "Token abc.def"})
    void getToken_invalidHeader_returnsNull(String header) {
        String result = jwtTokenUtil.getToken(header);
        assertThat(result).isNull();
    }

    @ParameterizedTest(name = "형식이 잘못된 토큰 [{0}]은 JwtException 발생한다")
    @ValueSource(strings = {"abc.def", "notajwt", "a.b.c.d"})
    void getEmail_withMalformedToken_throwsException(String malformed) {
        assertThatThrownBy(() -> jwtTokenUtil.getEmail(malformed))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 SignatureException 발생한다")
    void getEmail_withDifferentSecretKey_fails() {
        String validToken = jwtTokenUtil.createAccessToken(EMAIL);

        JwtProperties wrongProps = new JwtProperties();
        wrongProps.setSecret("different_secret_key_987654321123456");
        JwtTokenUtil wrongUtil = new JwtTokenUtil(wrongProps);

        assertThatThrownBy(() -> wrongUtil.getEmail(validToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("만료된 토큰을 검사하면 true를 반환한다")
    void isExpiredToken_withExpiredToken_returnsTrue() throws Exception {
        String token = jwtTokenUtil.generateToken(EMAIL, 100);
        TimeUnit.MILLISECONDS.sleep(150);
        assertThat(jwtTokenUtil.isExpiredToken(token)).isTrue();
    }

    @ParameterizedTest(name = "잘못된 토큰을 isExpiredToken에 넘기면 JwtException 발생한다")
    @ValueSource(strings = {"abc.def", "invalid.token.structure"})
    void isExpiredToken_withMalformedToken_throwsException(String invalidToken) {
        assertThatThrownBy(() -> jwtTokenUtil.isExpiredToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 토큰에서 getEmail은 예외, getExpiredEmail은 이메일 반환한다")
    void expiredToken_behavior_difference() throws Exception {
        String token = jwtTokenUtil.generateToken(EMAIL, 100);
        TimeUnit.MILLISECONDS.sleep(200);

        assertThatThrownBy(() -> jwtTokenUtil.getEmail(token))
                .isInstanceOf(ExpiredJwtException.class);

        assertThat(jwtTokenUtil.getExpiredEmail(token)).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("형식이 잘못된 토큰에서 getExpiredEmail은 JwtException 발생한다")
    void getExpiredEmail_withMalformedToken_throwsException() {
        assertThatThrownBy(() -> jwtTokenUtil.getExpiredEmail("invalid.token"))
                .isInstanceOf(JwtException.class);
    }

    @ParameterizedTest(name = "Refresh Token이 null 또는 빈 문자열이면 InvalidTokenException 발생한다")
    @NullAndEmptySource
    void reIssueRefreshToken_withEmptyToken_throwsException(String token) {
        assertThatThrownBy(() -> jwtTokenUtil.reIssueRefreshToken(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("not exist");
    }

    @Test
    @DisplayName("만료된 Refresh Token 재발급 시 InvalidTokenException 발생한다")
    void reIssueRefreshToken_withExpiredToken_throwsException() throws Exception {
        String expired = jwtTokenUtil.generateToken(EMAIL, 100);
        TimeUnit.MILLISECONDS.sleep(200);

        assertThatThrownBy(() -> jwtTokenUtil.reIssueRefreshToken(expired))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("남은 기간이 충분하면 재발급하지 않고 기존 토큰을 그대로 반환한다")
    void reIssueRefreshToken_whenHalfNotPassed_returnsSameToken() {
        String token = jwtTokenUtil.createRefreshToken(EMAIL).getRefreshToken();

        String result = jwtTokenUtil.reIssueRefreshToken(token);

        assertThat(result).isEqualTo(token);
    }
}
