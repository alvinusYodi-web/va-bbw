package com.bbw.vabbw;

import com.bbw.vabbw.entity.ApiClient;
import com.bbw.vabbw.repository.ApiClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bbw.vabbw.entity.VirtualAccount;
import com.bbw.vabbw.repository.VirtualAccountRepository;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataInitializer {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner initializeClient(
            ApiClientRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${bbw.client-password}") String clientPassword,
            @Value("${bbw.signature-secret}") String signatureSecret) {

        return args -> {
            if (repository.findByClientId("001").isEmpty()) {

                ApiClient client = new ApiClient();

                client.setClientId("001");
                client.setClientName("BBW Test Client");
                client.setUsername("bbwuser");

                client.setPassword(
                    passwordEncoder.encode(clientPassword)
                );

                client.setSignatureSecret(signatureSecret);
                client.setActive(true);

                repository.save(client);

                System.out.println("Data client awal berhasil dibuat.");
            }
        };
    }

    @Bean
    CommandLineRunner initializeVirtualAccount(
            VirtualAccountRepository repository) {

        return args -> {
            String vaNumber = "7771234567890";

            if (repository.findByVirtualAccountAndActiveTrue(vaNumber)
                    .isEmpty()) {

                VirtualAccount account = new VirtualAccount();
                account.setVirtualAccount(vaNumber);
                account.setAccountName("Febri Haryadi");
                account.setActive(true);

                repository.save(account);

                System.out.println(
                        "Data virtual account berhasil dibuat."
                );
            }
        };
    }
}