package com.bbw.vabbw.service;

import com.bbw.vabbw.entity.ApiClient;
import com.bbw.vabbw.entity.AuthToken;
import com.bbw.vabbw.repository.AuthTokenRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service
public class SignatureService {

    private final AuthTokenRepository authTokenRepository;
    private final ObjectMapper objectMapper;

    public SignatureService(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void validateSignature(
            String token,
            String timestamp,
            String providedSignature,
            Map<String, String> requestBody) {

        // 1. Periksa kelengkapan header
        if (token == null || token.isBlank()
                || timestamp == null || timestamp.isBlank()
                || providedSignature == null
                || providedSignature.isBlank()) {

            throw new RuntimeException("HEADER_INCOMPLETE");
        }

        // 2. Cari token aktif
        AuthToken authToken = authTokenRepository
                .findByTokenAndActiveTrue(token)
                .orElseThrow(() ->
                        new RuntimeException("INVALID_TOKEN"));

        // 3. Periksa masa berlaku token
        if (authToken.getExpiresAt() == null
                || !authToken.getExpiresAt().isAfter(Instant.now())) {

            throw new RuntimeException("INVALID_TOKEN");
        }

        ApiClient client = authToken.getApiClient();

        if (client == null || !client.isActive()) {
            throw new RuntimeException("INVALID_TOKEN");
        }

        // 4. Periksa timestamp
        Instant requestTime;

        try {
            requestTime = Instant.parse(timestamp);
        } catch (Exception e) {
            throw new RuntimeException("INVALID_TIMESTAMP");
        }

        long timeDifference = Math.abs(
                Duration.between(requestTime, Instant.now()).toSeconds()
        );

        // Maksimal selisih waktu 5 menit
        if (timeDifference > 300) {
            throw new RuntimeException("INVALID_TIMESTAMP");
        }
        try {
            // 5. Serialisasi body menjadi JSON
            String bodyJson = objectMapper.writeValueAsString(
                    requestBody
            );

            // 6. Bersihkan body sesuai dokumentasi perusahaan
            String canonicalBody = bodyJson
                    .replace("\r", "")
                    .replace("\n", "")
                    .replace("\t", "")
                    .replace(" ", "");

            // 7. Hash body menggunakan SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] bodyHash = digest.digest(
                    canonicalBody.getBytes(StandardCharsets.UTF_8)
            );

            String bodyHashHex = HexFormat.of().formatHex(bodyHash);

            // 8. Bentuk StringToSign
            String stringToSign =
                    token + ":" + bodyHashHex + ":" + timestamp;

            // 9. Hitung HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    client.getSignatureSecret()
                            .getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] calculatedSignature = mac.doFinal(
                    stringToSign.getBytes(StandardCharsets.UTF_8)
            );

            String expectedSignature =
                    HexFormat.of().formatHex(calculatedSignature);

            // 10. Bandingkan signature dengan aman
            boolean valid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.US_ASCII),
                    providedSignature.toLowerCase()
                            .getBytes(StandardCharsets.US_ASCII)
            );

            if (!valid) {
                throw new RuntimeException("INVALID_SIGNATURE");
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("INVALID_BODY");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("SIGNATURE_ERROR");
        }
    }
}