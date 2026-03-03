package com.neurelpress.blogs.utils;

import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor
public final class SecureTokenGenerator {

    private static final int DEFAULT_BYTE_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateHexToken() {
        return generateHexToken(DEFAULT_BYTE_LENGTH);
    }

    public static String generateHexToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(byteLength * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
