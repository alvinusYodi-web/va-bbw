package com.bbw.vabbw.controller;

import com.bbw.vabbw.service.InquiryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bbw.vabbw.service.SignatureService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/va")
public class InquiryController {

    private final InquiryService inquiryService;
    private final SignatureService signatureService;

    public InquiryController(
            InquiryService inquiryService,
            SignatureService signatureService) {

        this.inquiryService = inquiryService;
        this.signatureService = signatureService;
    }

    @PostMapping("/inquiry")
    public ResponseEntity<Map<String, Object>> inquiry(
            @RequestHeader(value = "Authorization", required = false)
            String authorization,

            @RequestHeader(value = "X-BBW-Timestamp", required = false)
            String timestamp,

            @RequestHeader(value = "X-BBW-Signature", required = false)
            String signature,

            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        // Periksa header Authorization
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            response.put("rc", "001");
            response.put("message", "Header not complete");

            return ResponseEntity.badRequest().body(response);
        }

        try {
            String token = authorization.substring(7);

            signatureService.validateSignature(
                token,
                timestamp,
                signature,
                request
            );

            String virtualAccount = request.get("virtual_account");

            if (virtualAccount == null || virtualAccount.isBlank()) {
                response.put("rc", "001");
                response.put("message", "Header not complete");

                return ResponseEntity.badRequest().body(response);
            }

            String accountName = inquiryService.getAccountName(
                token,
                virtualAccount,
                request.get("client_id")
            );

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("account_name", accountName);

            response.put("rc", "000");
            response.put("message", "Success");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            if ("HEADER_INCOMPLETE".equals(e.getMessage())) {
                response.put("rc", "001");
                response.put("message", "Header not complete");

                return ResponseEntity.badRequest().body(response);
            }

            if ("INVALID_SIGNATURE".equals(e.getMessage())) {
                response.put("rc", "004");
                response.put("message", "Signature not valid");

                return ResponseEntity.ok(response);
            }

            if ("INVALID_TOKEN".equals(e.getMessage())) {
                response.put("rc", "002");
                response.put("message", "Token not valid");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            if ("VA_NOT_FOUND".equals(e.getMessage())) {
                response.put("rc", "003");
                response.put("message", "VA not found");

                return ResponseEntity.ok(response);
            }

            if ("CLIENT_NOT_FOUND".equals(e.getMessage())) {
                response.put("rc", "005");
                response.put("message", "Client ID not found");

                return ResponseEntity.ok(response);
            }

            response.put("rc", "006");
            response.put("message", "General Error");

            return ResponseEntity.internalServerError().body(response);
        }
    }
}