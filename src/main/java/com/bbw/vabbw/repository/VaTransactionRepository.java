package com.bbw.vabbw.repository;

import com.bbw.vabbw.entity.VaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaTransactionRepository
        extends JpaRepository<VaTransaction, Long> {

    Optional<VaTransaction> findByClientIdAndReferenceNumber(
            String clientId,
            String referenceNumber
    );
}