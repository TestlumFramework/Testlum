package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.AuthUtil;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.http.HttpStatusCode;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CREDENTIALS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ENDPOINT_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.INVALID_CREDENTIALS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SERVER_BAD_GATEWAY_RESPONSE_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SERVER_ERROR_RESPONSE_LOG;
import static com.knubisoft.e2e.testing.framework.util.ResultUtil.AUTHENTICATION_TYPE;
import static java.util.Objects.nonNull;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {

    private static final String TYPE = "JWT";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        log.info(ALIAS_LOG, auth.getApiAlias());
        log.info(ENDPOINT_LOG, auth.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
        String body = prepareBody(auth);
        String token = getJwtToken(body, auth, result);
        result.put(AUTHENTICATION_TYPE, TYPE);
        login(token, BEARER_PREFIX);
    }

    @SneakyThrows
    private String getJwtToken(final String body, final Auth auth, final CommandResult result) {
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
            handleError(exception);
        }
        return DelimiterConstant.EMPTY;
    }

    private void handleError(final HttpClientErrorException exception) {
        if (HttpStatusCode.NOT_FOUND == exception.getRawStatusCode()) {
            log.info(INVALID_CREDENTIALS_LOG, exception.getRawStatusCode());
        } else if (HttpStatusCode.BAD_GATEWAY == exception.getRawStatusCode()) {
            log.info(SERVER_BAD_GATEWAY_RESPONSE_LOG, exception.getRawStatusCode());
        } else {
            log.info(SERVER_ERROR_RESPONSE_LOG, exception.getRawStatusCode());
        }
    }
}
