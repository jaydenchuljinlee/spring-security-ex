package com.example.security.config.security;

import com.example.security.config.security.filter.JwtTokenFilterFactory;
import com.example.security.config.security.handler.form.FormLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final JwtTokenFilterFactory jwtTokenFilterFactory;
    private final FormLoginSuccessHandler formLoginSuccessHandler;
    private final Environment environment;

    /**
     * 정적 리소스에 대한 보안 설정 (필요시)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDebugEnabled = Arrays.asList(activeProfiles).contains("dev")
                || Arrays.asList(activeProfiles).contains("local");

        return (web) -> {
            if (isDebugEnabled) {
                web.debug(true);
            }
            // 정적 리소스 제외 (필요시)
            // web.ignoring().requestMatchers("/static/**", "/css/**", "/js/**");
        };
    }

    /**
     * 보안 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // 인가 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/main",
                                "/login/**",
                                "/auth/**",
                                "/users"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 세션 관리 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 로그아웃 비활성화
                .logout(AbstractHttpConfigurer::disable)

                // Form 로그인 설정
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler(formLoginSuccessHandler)
                        .permitAll()
                )

                // JWT 필터 추가
                .addFilterBefore(
                        jwtTokenFilterFactory.getInstance(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080"
                // 프로덕션 도메인 추가
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 인증 정보 포함 여부
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        // 노출할 헤더 (응답 헤더 중 브라우저에서 접근 가능한 헤더)
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Refresh-Token"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
