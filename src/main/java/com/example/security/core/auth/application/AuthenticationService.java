package com.example.security.core.auth.application;

import com.example.security.core.auth.domain.exceptions.AuthenticationFailException;
import com.example.security.core.user.domain.entity.User;
import com.example.security.core.user.domain.repository.UserDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User authenticate(String email, String password) {
        Optional<User> userOptional = this.userDetailRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw AuthenticationFailException.userNotRegistered(email);
        }

        User user = userOptional.get();
        boolean passwordMatched = this.passwordEncoder.matches(password, user.getPassword());

        if (!passwordMatched) {
            throw AuthenticationFailException.passwordNotMatched(email);
        }

        return user;
    }
}
