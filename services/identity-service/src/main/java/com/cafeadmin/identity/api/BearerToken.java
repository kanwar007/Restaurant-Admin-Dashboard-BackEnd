package com.cafeadmin.identity.api;

public final class BearerToken {

    private static final String PREFIX = "bearer ";

    private BearerToken() {
    }

    public static String from(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }
        String value = authorizationHeader.trim();
        if (value.toLowerCase().startsWith(PREFIX)) {
            return value.substring(PREFIX.length()).trim();
        }
        return value.isBlank() ? null : value;
    }
}
