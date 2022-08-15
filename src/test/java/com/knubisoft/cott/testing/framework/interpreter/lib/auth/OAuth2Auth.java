package com.knubisoft.cott.testing.framework.interpreter.lib.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.AuthorizationConstant;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.global_config.Oauth2;
import com.knubisoft.cott.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static java.util.Objects.nonNull;

public class OAuth2Auth extends AbstractAuthStrategy {

    private final RestTemplate restTemplate = new RestTemplate();

    public OAuth2Auth(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        LogUtil.logAuthInfo(auth);
        String token = getOAuth2Token(auth);
        result.put(ResultUtil.AUTHENTICATION_TYPE, AuthorizationConstant.HEADER_OAUTH2);
        login(token, AuthorizationConstant.HEADER_BEARER);
    }

    private String getOAuth2Token(final Auth auth) {
        final Oauth2 oauth2 = GlobalTestConfigurationProvider.provide().getAuth().getOauth2();
        String authCode = getAuthorizationCode(auth, oauth2);
        return getAccessToken(auth, oauth2, authCode);
    }

    private String getAccessToken(final Auth auth, final Oauth2 oauth2, final String authCode) {
        RequestEntity<?> request = prepareTokenRequest(oauth2, auth, authCode);
        String response = executeRequest(request);

        if (nonNull(response) && !response.isEmpty()) {
            DocumentContext context = JsonPath.parse(response);
            return context.read(AuthorizationConstant.CONTENT_KEY_ACCESS_TOKEN);
        }
        return DelimiterConstant.EMPTY;
    }

    public RequestEntity<?> prepareTokenRequest(final Oauth2 oauth2, final Auth auth, final String authCode) {
        MultiValueMap<String, String> formParams = buildFormParameters(oauth2, auth, authCode);
        HttpHeaders headers = getTokenRequestHeaders(oauth2);
        URI uri = UriComponentsBuilder.fromUriString(oauth2.getAccessTokenUri()).build().toUri();
        return new RequestEntity<>(formParams, headers, HttpMethod.POST, uri);
    }

    private MultiValueMap<String, String> buildFormParameters(final Oauth2 oauth2,
                                                              final Auth auth,
                                                              final String authCode) {
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(AuthorizationConstant.GRANT_TYPE, AuthorizationConstant.AUTHORIZATION_CODE);
        formParameters.add(AuthorizationConstant.CODE, authCode);
        formParameters.add(AuthorizationConstant.REDIRECT_URI, getBaseApiUrl(auth) + auth.getLoginEndpoint());
        formParameters.add(AuthorizationConstant.SCOPE, oauth2.getScope());
        return formParameters;
    }

    private HttpHeaders getTokenRequestHeaders(final Oauth2 oauth2) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(oauth2.getClientId(), oauth2.getClientSecret());
        MediaType contentType = MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        headers.setContentType(contentType);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }

    private String executeRequest(final RequestEntity<?> request) {
        try {
            return restTemplate.exchange(request, String.class)
                    .getBody();
        } catch (HttpClientErrorException ex) {
            LogUtil.logResponseStatusError(ex);
        }
        return DelimiterConstant.EMPTY;
    }

    private String getBaseApiUrl(final Auth auth) {
        return GlobalTestConfigurationProvider.provide().getIntegrations().getApis().getApi().stream()
                .filter(api -> api.getAlias().equalsIgnoreCase(auth.getApiAlias()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Alias doesn't exist."))
                .getUrl();
    }

    @SneakyThrows
    private String getAuthorizationCode(final Auth auth, final Oauth2 oauth2) {
        //TODO impl
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.print("Enter authorization code:");
        return reader.readLine();
    }
}
