package com.digital_banking_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DigitalBankingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalBankingApiApplication.class, args);
    }

}
