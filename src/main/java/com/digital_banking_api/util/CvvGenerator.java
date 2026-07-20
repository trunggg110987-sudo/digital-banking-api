package com.digital_banking_api.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CvvGenerator {

    public String generateCVV() {

        SecureRandom random = new SecureRandom();

        return String.format("%03d", random.nextInt(1000));
    }

}