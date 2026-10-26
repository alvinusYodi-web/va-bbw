package com.bbw.vabbw.repository;

import com.bbw.vabbw.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiClientRepository extends JpaRepository<ApiClient, Long>{

    Optional<ApiClient> findByUsername(String username);
    Optional<ApiClient> findByClientId(String clientId);
    
} 