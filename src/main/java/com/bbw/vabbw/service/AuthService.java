package com.bbw.vabbw.service;

import com.bbw.vabbw.entity.ApiClient;
import com.bbw.vabbw.entity.AuthToken;
import com.bbw.vabbw.repository.ApiClientRepository;
import com.bbw.vabbw.repository.AuthTokenRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {

    private final ApiClientRepository apiClientRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            ApiClientRepository apiClientRepository,
            AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder) {

        this.apiClientRepository = apiClientRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateToken(String username, String password) {

        // 1. Cari client berdasarkan username
        ApiClient client = apiClientRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Invalid username or password"));

        // 2. Periksa apakah client aktif
        if (!client.isActive()) {
            throw new RuntimeException("Client is inactive");
        }

        // 3. Cocokkan password dengan password yang tersimpan
        if (!passwordEncoder.matches(password, client.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // 4. Di sini saya Buat token acak
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // 5. Hasil dari token acak di Simpan ke database
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setApiClient(client);
        authToken.setExpiresAt(Instant.now().plusSeconds(3600));
        authToken.setActive(true);

        authTokenRepository.save(authToken);

        return token;
    }
}