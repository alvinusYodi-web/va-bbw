package com.bbw.vabbw.service;

import com.bbw.vabbw.entity.ApiClient;
import com.bbw.vabbw.entity.AuthToken;
import com.bbw.vabbw.entity.VaTransaction;
import com.bbw.vabbw.repository.ApiClientRepository;
import com.bbw.vabbw.repository.AuthTokenRepository;
import com.bbw.vabbw.repository.VaTransactionRepository;
import com.bbw.vabbw.repository.VirtualAccountRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final AuthTokenRepository authTokenRepository;
    private final ApiClientRepository apiClientRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final VaTransactionRepository transactionRepository;

    public PaymentService(
            AuthTokenRepository authTokenRepository,
            ApiClientRepository apiClientRepository,
            VirtualAccountRepository virtualAccountRepository,
            VaTransactionRepository transactionRepository) {

        this.authTokenRepository = authTokenRepository;
        this.apiClientRepository = apiClientRepository;
        this.virtualAccountRepository = virtualAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public VaTransaction makePayment(
            String token,
            String clientId,
            String referenceNumber,
            String virtualAccount,
            String amount,
            String note) {

        // 1. Validasi token
        AuthToken authToken = authTokenRepository
                .findByTokenAndActiveTrue(token)
                .orElseThrow(() ->
                        new RuntimeException("INVALID_TOKEN"));

        // 2. Validasi masa berlaku token
        if (authToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("INVALID_TOKEN");
        }

        // 3. Validasi status client dan client_id
        ApiClient client = authToken.getApiClient();

        if (!client.isActive()
                || !client.getClientId().equals(clientId)) {
            throw new RuntimeException("CLIENT_NOT_FOUND");
        }

        // 4. Pastikan VA tersedia
        virtualAccountRepository
                .findByVirtualAccountAndActiveTrue(virtualAccount)
                .orElseThrow(() ->
                        new RuntimeException("VA_NOT_FOUND"));

        // 5. Validasi nominal pembayaran
        BigDecimal paymentAmount;

        try {
            paymentAmount = new BigDecimal(amount);
        } catch (Exception e) {
            throw new RuntimeException("INVALID_AMOUNT");
        }

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("INVALID_AMOUNT");
        }

        // 6. Cegah reference number dipakai dua kali
        if (transactionRepository
                .findByClientIdAndReferenceNumber(
                        clientId, referenceNumber)
                .isPresent()) {

            throw new RuntimeException("DUPLICATE_REFERENCE");
        }

        // 7. Buat transaksi
        VaTransaction transaction = new VaTransaction();

        transaction.setTransactionNumber(
                "TRX-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase()
        );

        transaction.setClientId(clientId);
        transaction.setReferenceNumber(referenceNumber);
        transaction.setVirtualAccount(virtualAccount);
        transaction.setAmount(paymentAmount);
        transaction.setNote(note);
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(Instant.now());

        // 8. Simpan transaksi ke database
        return transactionRepository.save(transaction);
    }
}