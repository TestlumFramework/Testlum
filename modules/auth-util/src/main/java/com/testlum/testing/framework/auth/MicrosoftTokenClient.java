package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.model.global_config.MicrosoftAuthStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Component
public class MicrosoftTokenClient extends AbstractOAuthTokenClient<MicrosoftAuthStrategy> {

    private static final Set<String> OAUTH_ERROR_CODES = Set.of(
            "invalid_request", "invalid_client", "invalid_grant", "unauthorized_client", "unsupported_grant_type",
            "invalid_scope", "access_denied", "temporarily_unavailable", "server_error", "admin_policy_enforced",
            "interaction_required", "login_required", "consent_required");

    @Autowired
    public MicrosoftTokenClient(final JacksonService jacksonService) {
        super(jacksonService);
    }

    MicrosoftTokenClient(final JacksonService jacksonService, final RestClient restClient) {
        super(jacksonService, restClient);
    }

    @Override
    protected String providerName() {
        return "Microsoft";
    }

    @Override
    protected Set<String> supportedErrorCodes() {
        return OAUTH_ERROR_CODES;
    }

    @Override
    protected void validateCredentials(final MicrosoftAuthStrategy configuration, final String refreshToken) {
        if (configuration == null || StringUtils.isBlank(configuration.getClientId())
                || StringUtils.isBlank(configuration.getClientSecret()) || StringUtils.isBlank(refreshToken)
                || StringUtils.isBlank(configuration.getTenant()) || StringUtils.isBlank(configuration.getScope())) {
            throw new DefaultFrameworkException(
                    "Microsoft token request requires non-empty clientId, clientSecret, "
                            + "tenant, scope and refreshToken");
        }
    }

    @Override
    protected String tokenEndpoint(final MicrosoftAuthStrategy configuration) {
        if (!configuration.getTenant().matches("[A-Za-z0-9][A-Za-z0-9.-]*")) {
            throw new DefaultFrameworkException("Microsoft auth strategy <%s> has an invalid tenant",
                    configuration.getAlias());
        }
        return "https://login.microsoftonline.com/" + configuration.getTenant() + "/oauth2/v2.0/token";
    }

    @Override
    protected MultiValueMap<String, String> createForm(final MicrosoftAuthStrategy configuration,
                                                    final String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", configuration.getClientId());
        form.add("client_secret", configuration.getClientSecret());
        form.add("refresh_token", refreshToken);
        form.add("scope", configuration.getScope());
        return form;
    }

}
