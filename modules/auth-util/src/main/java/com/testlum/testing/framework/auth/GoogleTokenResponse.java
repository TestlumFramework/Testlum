package com.testlum.testing.framework.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GoogleTokenResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;
}
