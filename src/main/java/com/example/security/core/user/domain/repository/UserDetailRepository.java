package com.example.security.core.user.domain.repository;

import com.example.security.core.user.domain.entity.User;

import java.util.Optional;

public interface UserDetailRepository {
    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(long userId);
}
