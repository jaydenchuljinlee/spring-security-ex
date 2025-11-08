package com.example.security.config.security.provider;

import com.example.security.core.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    // UserDetailsService는 Spring Security의 인터페이스를 사용합니다.
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 1. UserDetailsService를 통해 사용자 정보 조회
        // User 엔티티가 UserDetails를 구현해야 합니다.
        User user = (User) userDetailsService.loadUserByUsername(username);

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        // 3. 인증 성공 시, 인증된 Authentication 객체 반환
        // 생성자: principal, credentials, authorities
        // 성공 후에는 비밀번호(credentials)를 null로 설정하는 것이 안전합니다.
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 이 Provider가 UsernamePasswordAuthenticationToken 타입의 인증을 처리하도록 명시
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
