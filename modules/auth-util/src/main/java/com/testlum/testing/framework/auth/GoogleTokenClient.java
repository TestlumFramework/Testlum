package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Component
public class GoogleTokenClient extends AbstractOAuthTokenClient<GoogleAuthStrategy> {

    private static final Set<String> OAUTH_ERROR_CODES = Set.of(
            "invalid_request", "invalid_client", "invalid_grant", "unauthorized_client", "unsupported_grant_type",
            "invalid_scope", "access_denied", "temporarily_unavailable", "server_error", "admin_policy_enforced",
            "invalid_rapt");

    @Autowired
    public GoogleTokenClient(final JacksonService jacksonService) {
        super(jacksonService);
    }

    GoogleTokenClient(final JacksonService jacksonService, final RestClient restClient) {
        super(jacksonService, restClient);
    }

    @Override
    protected String providerName() {
        return "Google";
    }

    @Override
    protected Set<String> supportedErrorCodes() {
        return OAUTH_ERROR_CODES;
    }

    @Override
    protected void validateCredentials(final GoogleAuthStrategy configuration, final String refreshToken) {
        if (configuration == null || StringUtils.isBlank(configuration.getClientId())
                || StringUtils.isBlank(configuration.getClientSecret()) || StringUtils.isBlank(refreshToken)) {
            throw new DefaultFrameworkException(
                    "Google token request requires non-empty clientId, clientSecret and refreshToken");
        }
    }

    @Override
    protected MultiValueMap<String, String> createForm(final GoogleAuthStrategy configuration,
                                                    final String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", configuration.getClientId());
        form.add("client_secret", configuration.getClientSecret());
        form.add("refresh_token", refreshToken);
        return form;
    }

    @Override
    protected String tokenEndpoint(final GoogleAuthStrategy configuration) {
        return "https://oauth2.googleapis.com/token";
    }

}
