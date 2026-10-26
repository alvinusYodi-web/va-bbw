package com.bbw.vabbw.controller;

import com.bbw.vabbw.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(
            value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<Map<String, Object>> generateToken(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,

            @RequestParam(
                    value = "grant_type",
                    required = false
            ) String grantType) {

        // Siapkan format respons
        Map<String, Object> response = new LinkedHashMap<>();

        // 1. Periksa header Authorization
        if (authorization == null || !authorization.startsWith("Basic ")) {
            response.put("rc", "001");
            response.put("message", "Header not complete");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        // 2. Periksa grant_type
        if (grantType == null ||
                !(grantType.equals("client_credential")
                        || grantType.equals("client_credentials"))) {

            response.put("rc", "006");
            response.put("message", "Invalid grant_type");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        try {
            // 3. Ambil username dan password dari Basic Auth
            String encodedCredentials =
                    authorization.substring("Basic ".length());

            byte[] decodedBytes =
                    Base64.getDecoder().decode(encodedCredentials);

            String credentials =
                    new String(decodedBytes, StandardCharsets.UTF_8);

            int separator = credentials.indexOf(':');

            if (separator < 0) {
                throw new IllegalArgumentException(
                        "Invalid Authorization header");
            }

            String username = credentials.substring(0, separator);
            String password = credentials.substring(separator + 1);

            // 4. Buat token melalui service
            String token = authService.generateToken(username, password);

        // 5. Kirim token kepada client
        //     response.put("rc", "000");
        //     response.put("message", "Success");
            response.put("auth_token", token);
        //     response.put("access_token", token);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("rc", "006");
            response.put("message", "Authentication failed");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }
}