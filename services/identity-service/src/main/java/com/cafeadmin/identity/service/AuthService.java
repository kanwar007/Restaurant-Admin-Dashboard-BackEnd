package com.cafeadmin.identity.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafeadmin.common.web.ApiException;
import com.cafeadmin.identity.api.dto.LoginRequest;
import com.cafeadmin.identity.domain.StaffUser;
import com.cafeadmin.identity.domain.UserSession;
import com.cafeadmin.identity.repo.StaffUserRepository;
import com.cafeadmin.identity.repo.UserSessionRepository;

@Service
public class AuthService {

    private final StaffUserRepository users;
    private final UserSessionRepository sessions;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Duration sessionTtl;

    public AuthService(StaffUserRepository users, UserSessionRepository sessions,
                       @Value("${app.session-ttl-hours:12}") long sessionTtlHours) {
        this.users = users;
        this.sessions = sessions;
        this.sessionTtl = Duration.ofHours(sessionTtlHours);
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        StaffUser user = users.findByUsernameAndActiveTrue(request.username())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));

        String token = UUID.randomUUID().toString();
        sessions.save(new UserSession(user.getId(), hash(token), Instant.now().plus(sessionTtl)));
        return new LoginResult(token, user);
    }

    @Transactional
    public Optional<StaffUser> currentUser(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return sessions.findByTokenHash(hash(token))
                .filter(session -> session.getRevokedAt() == null && session.getExpiresAt().isAfter(Instant.now()))
                .map(session -> {
                    session.touch();
                    return session.getUserId();
                })
                .flatMap(users::findById);
    }

    @Transactional
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        sessions.findByTokenHash(hash(token)).ifPresent(UserSession::revoke);
    }

    public StaffUser requireUser(String token) {
        return currentUser(token).orElseThrow(() -> ApiException.unauthorized("Not signed in"));
    }

    private static byte[] hash(String token) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record LoginResult(String token, StaffUser user) {
    }
}
