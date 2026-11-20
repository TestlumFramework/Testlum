package com.testlum.testing.framework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenMasker {

    private static final int VISIBLE_CHARACTERS = 3;
    private static final String MASK = "***";

    public static String mask(final String token) {
        return token == null || token.length() <= VISIBLE_CHARACTERS
                ? MASK : token.substring(0, VISIBLE_CHARACTERS) + MASK;
    }

    public static String maskAuthorization(final String value) {
        int separator = value == null ? -1 : value.indexOf(' ');
        return separator < 0 ? mask(value)
                : value.substring(0, separator + 1) + mask(value.substring(separator + 1));
    }
}
