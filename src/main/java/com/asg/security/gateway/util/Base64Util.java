package com.asg.security.gateway.util;

import java.util.Base64;

public final class Base64Util {

    private Base64Util() {
    }

    public static String encode(String value) {
        try {
            return Base64.getEncoder().encodeToString(value.getBytes());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot encode, check string again.", e);
        }
    }

    public static String decode(String value) {
        try {
            return new String(Base64.getDecoder().decode(value));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot decode, check string again.", e);
        }
    }
}

