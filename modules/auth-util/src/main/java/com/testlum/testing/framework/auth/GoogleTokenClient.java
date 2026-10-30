package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Component
public class GoogleTokenClient {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final Set<String> OAUTH_ERROR_CODES = Set.of(
            "invalid_request", "invalid_client", "invalid_grant", "unauthorized_client", "unsupported_grant_type",
            "invalid_scope", "access_denied", "temporarily_unavailable", "server_error", "admin_policy_enforced",
            "invalid_rapt");

    private final JacksonService jacksonService;
    private final RestClient restClient;

    @Autowired
    public GoogleTokenClient(final JacksonService jacksonService) {
        this(jacksonService, createRestClient());
    }

    GoogleTokenClient(final JacksonService jacksonService, final RestClient restClient) {
        this.jacksonService = jacksonService;
        this.restClient = restClient;
    }

    public GoogleTokenResponse refreshToken(final GoogleAuthStrategy configuration, final String refreshToken) {
        validateCredentials(configuration, refreshToken);
        String response = requestToken(configuration, refreshToken);
        return parseTokenResponse(response, configuration.getAlias());
    }

    private static RestClient createRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return RestClient.builder().requestFactory(factory).build();
    }

    private void validateCredentials(final GoogleAuthStrategy configuration, final String refreshToken) {
        if (configuration == null || StringUtils.isBlank(configuration.getClientId())
                || StringUtils.isBlank(configuration.getClientSecret()) || StringUtils.isBlank(refreshToken)) {
            throw new DefaultFrameworkException(
                    "Google token request requires non-empty clientId, clientSecret and refreshToken");
        }
    }

    private MultiValueMap<String, String> createForm(final GoogleAuthStrategy configuration,
                                                    final String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", configuration.getClientId());
        form.add("client_secret", configuration.getClientSecret());
        form.add("refresh_token", refreshToken);
        return form;
    }

    private String requestToken(final GoogleAuthStrategy configuration, final String refreshToken) {
        try {
            return restClient.post().uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
                    .body(createForm(configuration, refreshToken)).retrieve().body(String.class);
        } catch (RestClientResponseException exception) {
            throw oauthFailure(configuration.getAlias(), exception.getStatusCode().value(),
                    extractErrorCode(exception.getResponseBodyAsString()));
        } catch (RestClientException exception) {
            throw new DefaultFrameworkException(
                    "Google token request failed for auth strategy <%s>: transport error", configuration.getAlias());
        }
    }

    private GoogleTokenResponse parseTokenResponse(final String response, final String alias) {
        Map<?, ?> values = parseResponseObject(response, alias);
        if (values.containsKey("error")) {
            throw oauthFailure(alias, HttpStatus.OK.value(), knownErrorCode(values.get("error")));
        }
        String accessToken = requireString(values, "access_token", alias);
        String tokenType = requireString(values, "token_type", alias);
        if (!"Bearer".equalsIgnoreCase(tokenType)) {
            throw new DefaultFrameworkException("Google token response for auth strategy <%s> must use Bearer", alias);
        }
        return new GoogleTokenResponse(accessToken, "Bearer", requireExpiresIn(values, alias));
    }

    private Map<?, ?> parseResponseObject(final String response, final String alias) {
        if (StringUtils.isBlank(response)) {
            throw new DefaultFrameworkException("Google token response is empty for auth strategy <%s>", alias);
        }
        Object parsed = parseJson(response, alias);
        if (!(parsed instanceof Map<?, ?> values)) {
            throw new DefaultFrameworkException("Google token response must be a JSON object for strategy <%s>", alias);
        }
        return values;
    }

    private Object parseJson(final String response, final String alias) {
        try {
            return jacksonService.readValue(response, Object.class);
        } catch (DefaultFrameworkException exception) {
            throw new DefaultFrameworkException("Google token response contains invalid JSON for strategy <%s>", alias);
        }
    }

    private String requireString(final Map<?, ?> values, final String field, final String alias) {
        Object value = values.get(field);
        if (!(value instanceof String text) || StringUtils.isBlank(text)) {
            throw new DefaultFrameworkException(
                    "Google token response for strategy <%s> requires a non-empty string %s", alias, field);
        }
        return text;
    }

    private long requireExpiresIn(final Map<?, ?> values, final String alias) {
        Object value = values.get("expires_in");
        if (!(value instanceof Integer || value instanceof Long) || ((Number) value).longValue() <= 0) {
            throw new DefaultFrameworkException(
                    "Google token response for strategy <%s> requires a positive integer expires_in", alias);
        }
        return ((Number) value).longValue();
    }

    private String extractErrorCode(final String response) {
        if (StringUtils.isBlank(response)) {
            return "unknown_error";
        }
        try {
            Object parsed = jacksonService.readValue(response, Object.class);
            if (parsed instanceof Map<?, ?> values) {
                return knownErrorCode(values.get("error"));
            }
        } catch (DefaultFrameworkException exception) {
            return "unknown_error";
        }
        return "unknown_error";
    }

    private String knownErrorCode(final Object error) {
        return error instanceof String code && OAUTH_ERROR_CODES.contains(code) ? code : "unknown_error";
    }

    private DefaultFrameworkException oauthFailure(final String alias, final int status, final String errorCode) {
        return new DefaultFrameworkException(
                "Google token request failed for auth strategy <%s>: HTTP %d (%s)", alias, status, errorCode);
    }
}
