package com.knubisoft.testlum.testing.framework.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationConstantTest {

    @Test
    void constantsHaveExpectedValues() {
        assertEquals("$.username", AuthorizationConstant.USERNAME_JPATH);
        assertEquals("$.password", AuthorizationConstant.PASSWORD_JPATH);
        assertEquals("token", AuthorizationConstant.CONTENT_KEY_TOKEN);
        assertEquals("Authorization", AuthorizationConstant.HEADER_AUTHORIZATION);
        assertEquals("Bearer ", AuthorizationConstant.HEADER_BEARER);
        assertEquals("Basic ", AuthorizationConstant.HEADER_BASIC);
        assertEquals("JWT", AuthorizationConstant.HEADER_JWT);
    }

    @Test
    void bearerPrefixEndsWithSpace() {
        assertTrue(AuthorizationConstant.HEADER_BEARER.endsWith(" "));
    }

    @Test
    void basicPrefixEndsWithSpace() {
        assertTrue(AuthorizationConstant.HEADER_BASIC.endsWith(" "));
    }
}
