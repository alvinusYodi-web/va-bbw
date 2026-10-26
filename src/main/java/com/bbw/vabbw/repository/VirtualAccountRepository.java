package com.bbw.vabbw.repository;

import com.bbw.vabbw.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VirtualAccountRepository
        extends JpaRepository<VirtualAccount, Long> {

    Optional<VirtualAccount> findByVirtualAccountAndActiveTrue(
            String virtualAccount
    );
}