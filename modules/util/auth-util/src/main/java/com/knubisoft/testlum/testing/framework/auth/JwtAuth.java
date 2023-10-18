package com.knubisoft.testlum.testing.framework.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    private static final String CREDENTIALS_LOG = format(TABLE_FORMAT, "Credentials", "{}");
    private static final String INVALID_CREDENTIALS_LOG = format(TABLE_FORMAT, "Invalid credentials", "{}");
    private static final String SERVER_BAD_GATEWAY_RESPONSE_LOG = format(TABLE_FORMAT, "Server is shutdown", "{}");
    private static final String SERVER_ERROR_RESPONSE_LOG = format(TABLE_FORMAT, "Request failed", "{}");
    private static final int NOT_FOUND = 404;
    private static final int BAD_GATEWAY = 502;

    //RESULT
    private static final String AUTHENTICATION_TYPE = "Authentication type";

    private final IntegrationsProvider integrationsProvider;
    private final List<Api> apiList;

    public JwtAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
        this.apiList = integrationsProvider.findListByEnv(Api.class, dependencies.getEnvironment());
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        logAuthInfo(auth);
        String token = getJwtToken(auth);
        result.put(AUTHENTICATION_TYPE, AuthorizationConstant.HEADER_JWT);
        login(token, AuthorizationConstant.HEADER_BEARER);
    }

    private String getJwtToken(final Auth auth) {
        String body = prepareBody(auth);
        HttpHeaders headers = getHeaders();
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String response = doRequest(auth, request);
        if (isNotBlank(response)) {
            return getTokenFromResponse(auth, response);
        }
        return DelimiterConstant.EMPTY;
    }

    private String getTokenFromResponse(final Auth auth, final String response) {
        DocumentContext context = JsonPath.parse(response);
        Api apiIntegration = integrationsProvider.findApiForAlias(apiList, auth.getApiAlias());
        return context.read(isNotBlank(apiIntegration.getAuth().getTokenName())
                ? apiIntegration.getAuth().getTokenName() : AuthorizationConstant.CONTENT_KEY_TOKEN);
    }

    private String prepareBody(final Auth auth) {
        return getCredentialsFromFile(auth.getCredentials());
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }

    private String doRequest(final Auth auth, final HttpEntity<String> request) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.postForObject(getFullApiUrl(auth), request, String.class);
        } catch (HttpClientErrorException exception) {
            logResponseStatusError(exception);
        }
        return DelimiterConstant.EMPTY;
    }

    private String getFullApiUrl(final Auth auth) {
        Api apiIntegration = integrationsProvider.findApiForAlias(apiList, auth.getApiAlias());
        return apiIntegration.getUrl() + auth.getLoginEndpoint();
    }

    @SneakyThrows
    private String getCredentialsFromFile(final String fileName) {
        return FileUtils.readFileToString(FileSearcher.searchFileFromDataFolder(fileName), StandardCharsets.UTF_8);
    }

    private void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, auth.getApiAlias());
        log.info(ENDPOINT_LOG, auth.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }

    private void logResponseStatusError(final HttpClientErrorException exception) {
        if (NOT_FOUND == exception.getRawStatusCode()) {
            log.info(INVALID_CREDENTIALS_LOG, exception.getRawStatusCode());
        } else if (BAD_GATEWAY == exception.getRawStatusCode()) {
            log.info(SERVER_BAD_GATEWAY_RESPONSE_LOG, exception.getRawStatusCode());
        } else {
            log.info(SERVER_ERROR_RESPONSE_LOG, exception.getRawStatusCode());
        }
    }
}
