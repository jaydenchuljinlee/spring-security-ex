package com.example.security.comn.utils;

import com.example.security.comn.properties.JwtProperties;
import com.example.security.core.auth.domain.exceptions.InvalidTokenException;
import com.example.security.core.auth.dto.RefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenUtil {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final String EMAIL_CLAIM_KEY = "email";

    private static final long ONE_MINUTE = 60_000L;
    private static final long ONE_HOUR = ONE_MINUTE * 60;
    private static final long ONE_DAY = ONE_HOUR * 24;

    private static final long ACCESS_TOKEN_EXPIRATION_TIME = ONE_MINUTE * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = ONE_DAY * 7; // 7일
    private static final long REFRESH_TOKEN_REISSUE_THRESHOLD = ONE_DAY * 4; // 4일

    private final JwtProperties jwtProperties;

    /**
     * Access Token 생성
     */
    public String createAccessToken(String email) {
        return generateToken(email, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    /**
     * Refresh Token 생성
     */
    public RefreshToken createRefreshToken(String email) {
        String token = generateToken(email, REFRESH_TOKEN_EXPIRATION_TIME);
        return RefreshToken.of(email, token, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    /**
     * JWT 토큰 생성
     */
    public String generateToken(String email, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claim(EMAIL_CLAIM_KEY, email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmail(String token) {
        Claims claims = extractClaims(token);
        return claims.get(EMAIL_CLAIM_KEY, String.class);
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpiredToken(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Refresh Token 재발급 (필요시)
     */
    public String reIssueRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException("Refresh Token not exist");
        }

        if (isExpiredToken(token)) {
            throw new InvalidTokenException("Refresh Token was expired");
        }

        // 남은 시간이 절반 이하면 재발급
        if (!isPassedByHalfTime(token)) {
            return token;
        }

        String email = getEmail(token);
        log.info("Refresh Token 재발급: {}", email);

        return generateToken(email, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    /**
     * 토큰 유효기간의 절반이 지났는지 확인
     */
    public boolean isPassedByHalfTime(String token) {
        long remainingTime = getRemainMilliSeconds(token);
        return remainingTime < REFRESH_TOKEN_REISSUE_THRESHOLD;
    }

    /**
     * Authorization 헤더에서 토큰 추출
     */
    public String getToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX_LENGTH);
    }

    /**
     * 만료된 토큰에서 이메일 추출 (재발급용)
     */
    public String getExpiredEmail(String token) {
        try {
            return getEmail(token);
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰에서 이메일 추출");
            return e.getClaims().get(EMAIL_CLAIM_KEY, String.class);
        }
    }

    /**
     * 토큰의 남은 유효시간 (밀리초)
     */
    public long getRemainMilliSeconds(String token) {
        Date expiration = extractClaims(token).getExpiration();
        Date now = new Date();
        return Math.max(0, expiration.getTime() - now.getTime());
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 서명 키 생성
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
