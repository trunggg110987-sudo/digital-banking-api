package com.digital_banking_api.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TransferReferenceGenerator {

    public String generateReferenceNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder referenceNumber = new StringBuilder("TRF");

        for (int i = 0; i < 12; i++) {
            referenceNumber.append(random.nextInt(10));
        }

        return referenceNumber.toString();
    }
}
