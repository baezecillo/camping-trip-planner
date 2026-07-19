package com.campingtripplanner.backend.service;

import com.campingtripplanner.backend.domain.User;
import com.campingtripplanner.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        // Relies on the DB-level unique constraint on users.username: a duplicate
        // username throws DataIntegrityViolationException, mapped to 409 globally.
        return userRepository.save(user);
    }
}
