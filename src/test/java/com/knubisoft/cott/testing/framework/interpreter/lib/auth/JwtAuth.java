package com.knubisoft.cott.testing.framework.interpreter.lib.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.AuthorizationConstant;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.AuthUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.knubisoft.cott.testing.framework.util.ResultUtil.AUTHENTICATION_TYPE;
import static java.util.Objects.nonNull;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {


    public JwtAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        LogUtil.logAuthInfo(auth);
        String body = prepareBody(auth);
        String token = getJwtToken(body, auth);
        result.put(AUTHENTICATION_TYPE, AuthorizationConstant.HEADER_JWT);
        login(token, AuthorizationConstant.HEADER_BEARER);
    }

    @SneakyThrows
    private String getJwtToken(final String body, final Auth auth) {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String response = doRequest(auth, request);
        if (nonNull(response) && !response.isEmpty()) {
            DocumentContext context = JsonPath.parse(response);
            return context.read(AuthorizationConstant.CONTENT_KEY_TOKEN);
        }
        return DelimiterConstant.EMPTY;
    }

    @SneakyThrows
    private String prepareBody(final Auth auth) {
        return AuthUtil.getCredentialsFromFile(auth.getCredentials());
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

    private String doRequest(final Auth auth, final HttpEntity<String> request) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.postForObject(getBaseApiUrl(auth) + auth.getLoginEndpoint(),
                    request, String.class);
        } catch (HttpClientErrorException exception) {
            LogUtil.logResponseStatusError(exception);
        }
        return DelimiterConstant.EMPTY;
    }
}
