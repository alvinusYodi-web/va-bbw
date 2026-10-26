package com.bbw.vabbw.service;

import com.bbw.vabbw.entity.AuthToken;
import com.bbw.vabbw.entity.VirtualAccount;
import com.bbw.vabbw.repository.AuthTokenRepository;
import com.bbw.vabbw.repository.VirtualAccountRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
// import java.util.Optional;

@Service
public class InquiryService {

    private final AuthTokenRepository authTokenRepository;
    private final VirtualAccountRepository virtualAccountRepository;

    public InquiryService(
            AuthTokenRepository authTokenRepository,
            VirtualAccountRepository virtualAccountRepository) {
        this.authTokenRepository = authTokenRepository;
        this.virtualAccountRepository = virtualAccountRepository;
    }

    public String getAccountName(
        String token, 
        String virtualAccount,
        String clientId) {

        // 1. Validasi token
        AuthToken authToken = authTokenRepository
                .findByTokenAndActiveTrue(token)
                .orElseThrow(() ->
                        new RuntimeException("INVALID_TOKEN"));

        // 2. Periksa masa berlaku token
        if (authToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("INVALID_TOKEN");
        }

        // 3. Periksa status client
        if (!authToken.getApiClient().isActive()) {
            throw new RuntimeException("INVALID_TOKEN");
        }

        // 4. Periksa client ID
        if (!authToken.getApiClient().getClientId().equals(clientId)) {
            throw new RuntimeException("CLIENT_NOT_FOUND");
        }

        // 5. Cari virtual account yang aktif
        VirtualAccount account = virtualAccountRepository
                .findByVirtualAccountAndActiveTrue(virtualAccount)
                .orElseThrow(() ->
                        new RuntimeException("VA_NOT_FOUND"));

        // 5. Kembalikan nama pemilik VA
        return account.getAccountName();
    }
}