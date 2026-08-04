package com.digital_banking_api.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CvvHasher {

    private static final String ALGORITHM = "SHA-256";

    public static String hashCVV(String cvv) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(cvv.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash CVV", e);
        }
    }

    public static boolean verifyCVV(String plainCVV, String hashedCVV) {
        return hashCVV(plainCVV).equals(hashedCVV);
    }
}
