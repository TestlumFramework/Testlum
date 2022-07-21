package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.AuthUtil;
import com.knubisoft.e2e.testing.model.global_config.Oauth2;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;

public class OAuth2Auth extends AbstractAuthStrategy {

    public OAuth2Auth(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(Auth auth, CommandResult result) {
        String body = prepareBody(auth);
        String token = getJwtToken(body, auth);
        login(token, AuthorizationConstant.HEADER_BEARER);
    }

    private String prepareBody(final Auth auth) {
        return AuthUtil.getCredentialsFromFile(auth.getCredentials());
    }

    private String getJwtToken(final String body, final Auth auth) {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        OAuth2RestTemplate oAuth2RestTemplate = getOauthRestTemplate();
        return oAuth2RestTemplate.postForObject(getBaseApiUrl(auth) + auth.getLoginEndpoint(),
                request, String.class);
    }

    private OAuth2RestTemplate getOauthRestTemplate() {
        AuthorizationCodeResourceDetails authorizationCodeResourceDetails = getAuthorizationCodeResourceDetails();
        return new OAuth2RestTemplate(authorizationCodeResourceDetails);
    }

    private AuthorizationCodeResourceDetails getAuthorizationCodeResourceDetails() {
        Oauth2 oauth2 = GlobalTestConfigurationProvider.provide().getAuth().getOauth2();
        AuthorizationCodeResourceDetails authorizationCodeResourceDetails = new AuthorizationCodeResourceDetails();
        authorizationCodeResourceDetails.setClientId(oauth2.getClientId());
        authorizationCodeResourceDetails.setClientSecret(oauth2.getClientSecret());
        authorizationCodeResourceDetails.setAccessTokenUri(oauth2.getTokenAccessUri());
        authorizationCodeResourceDetails.setClientAuthenticationScheme(AuthenticationScheme.form);
        return authorizationCodeResourceDetails;
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
