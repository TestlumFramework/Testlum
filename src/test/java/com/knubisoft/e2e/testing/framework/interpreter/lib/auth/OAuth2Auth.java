package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.AuthUtil;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;

public class OAuth2Auth extends AbstractAuthStrategy {

    private final InterpreterDependencies interpreterDependencies;

    public OAuth2Auth(InterpreterDependencies dependencies) {
        super(dependencies);
        this.interpreterDependencies = dependencies;
    }

    @Override
    public void authenticate(Auth auth, CommandResult result) {
        String body = prepareBody(auth);
        String token = getJwtToken(body, auth);
        login(token, "OAUTH2");
    }

    private OAuth2RestTemplate getOauthRestTemplate(final Auth auth) {
        AuthorizationCodeResourceDetails authorizationCodeResourceDetails = getAuthorizationCodeResourceDetails(auth);
        return new OAuth2RestTemplate(authorizationCodeResourceDetails);
    }

    private AuthorizationCodeResourceDetails getAuthorizationCodeResourceDetails(final Auth auth) {
        AuthorizationCodeResourceDetails authorizationCodeResourceDetails = new AuthorizationCodeResourceDetails();
        authorizationCodeResourceDetails.setClientId(auth.getOauth().getCliendId());
        authorizationCodeResourceDetails.setClientSecret(auth.getOauth().getCliendSecret());
        authorizationCodeResourceDetails.setAccessTokenUri(auth.getOauth().getAccesTokenUri());
        authorizationCodeResourceDetails.setClientAuthenticationScheme(AuthenticationScheme.form);
        return authorizationCodeResourceDetails;
    }

    @SneakyThrows
    private String prepareBody(final Auth auth) {
        return AuthUtil.getCredentialsFromFile(
                interpreterDependencies.getFileSearcher(), auth.getCredentials()
        );
    }

    @SneakyThrows
    private String getJwtToken(final String body, final Auth auth) {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        OAuth2RestTemplate oAuth2RestTemplate = getOauthRestTemplate(auth);
        return oAuth2RestTemplate.postForObject(getBaseApiUrl(auth) + auth.getLoginEndpoint(),
                request, String.class);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getBaseApiUrl(final Auth auth) {
        return GlobalTestConfigurationProvider.provide().getIntegrations().getApis().getApi().stream()
                .filter(api -> api.getAlias().equalsIgnoreCase(auth.getApiAlias()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Alias doesn't exist."))
                .getUrl();
    }
}
