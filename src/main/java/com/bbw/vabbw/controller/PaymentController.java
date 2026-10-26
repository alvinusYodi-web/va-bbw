package com.bbw.vabbw.controller;

import com.bbw.vabbw.entity.VaTransaction;
import com.bbw.vabbw.service.PaymentService;
import com.bbw.vabbw.service.SignatureService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/va")
public class PaymentController {

    private final PaymentService paymentService;
    private final SignatureService signatureService;

    public PaymentController(
            PaymentService paymentService,
            SignatureService signatureService) {

        this.paymentService = paymentService;
        this.signatureService = signatureService;
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> payment(
            @RequestHeader(value = "Authorization", required = false)
            String authorization,

            @RequestHeader(value = "X-BBW-Timestamp", required = false)
            String timestamp,

            @RequestHeader(value = "X-BBW-Signature", required = false)
            String signature,

            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            response.put("rc", "001");
            response.put("message", "Header not complete");

            return ResponseEntity.badRequest().body(response);
        }

        String clientId = request.get("client_id");
        String referenceNumber = request.get("reference_number");
        String virtualAccount = request.get("virtual_account");
        String amount = request.get("amount");
        String note = request.get("note");

        if (clientId == null || clientId.isBlank()
                || referenceNumber == null || referenceNumber.isBlank()
                || virtualAccount == null || virtualAccount.isBlank()
                || amount == null || amount.isBlank()) {

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

            VaTransaction transaction = paymentService.makePayment(
                    token,
                    clientId,
                    referenceNumber,
                    virtualAccount,
                    amount,
                    note
            );

            response.put("rc", "000");
            response.put("message", "Success");

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("transaction_number",
                    transaction.getTransactionNumber());

            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            String error = e.getMessage();

            if ("HEADER_INCOMPLETE".equals(error)) {
                response.put("rc", "001");
                response.put("message", "Header not complete");

                return ResponseEntity.badRequest().body(response);
            }

            if ("INVALID_SIGNATURE".equals(error)
                    || "INVALID_TIMESTAMP".equals(error)) {

                response.put("rc", "004");
                response.put("message", "Signature not valid");

                return ResponseEntity.ok(response);
            }

            if ("INVALID_TOKEN".equals(error)) {
                response.put("rc", "002");
                response.put("message", "Token not valid");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            if ("CLIENT_NOT_FOUND".equals(error)) {
                response.put("rc", "005");
                response.put("message", "Client ID not found");

                return ResponseEntity.ok(response);
            }

            if ("VA_NOT_FOUND".equals(error)) {
                response.put("rc", "003");
                response.put("message", "VA not found");

                return ResponseEntity.ok(response);
            }

            if ("INVALID_AMOUNT".equals(error)) {
                response.put("rc", "006");
                response.put("message", "Invalid payment amount");

                return ResponseEntity.badRequest().body(response);
            }

            if ("DUPLICATE_REFERENCE".equals(error)) {
                response.put("rc", "006");
                response.put("message", "Duplicate reference number");

                return ResponseEntity.ok(response);
            }

            response.put("rc", "006");
            response.put("message", "General Error");

            return ResponseEntity.internalServerError().body(response);
        }
    }
}