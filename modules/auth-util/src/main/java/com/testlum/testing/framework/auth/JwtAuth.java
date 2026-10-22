package com.testlum.testing.framework.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.testlum.log.LogFormat;
import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.SystemVariableService;
import com.testlum.testing.model.global_config.JwtAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {

    private static final String SERVER_ERROR_RESPONSE_LOG = LogFormat.table("Request failed");
    private final FileSearcher fileSearcher;
    private final SystemVariableService systemVariableService;
    private final JwtAuthStrategy configuration;

    public JwtAuth(final InterpreterDependencies dependencies,
                   final JwtAuthStrategy configuration) {
        super(dependencies);
        this.configuration = configuration;
        this.fileSearcher = dependencies.getContext().getBean(FileSearcher.class);
        this.systemVariableService = dependencies.getContext().getBean(SystemVariableService.class);
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
        String response = doRequest(request);
        if (StringUtils.isBlank(response)) {
            throw new DefaultFrameworkException(
                    "Login response is empty for auth strategy <%s>", configuration.getAlias());
        }
        return getTokenFromResponse(response);
    }

    private String getTokenFromResponse(final String response) {
        DocumentContext context = JsonPath.parse(response);
        Object value = context.read(getTokenPath());
        if (!(value instanceof String token) || StringUtils.isBlank(token)) {
            throw new DefaultFrameworkException("Login response must contain a non-empty string token "
                    + "for auth strategy <%s>", configuration.getAlias());
        }
        return token;
    }

    private String getTokenPath() {
        String tokenName = StringUtils.defaultIfBlank(configuration.getTokenName(), AuthorizationConstant.CONTENT_KEY_TOKEN);
        if (tokenName.startsWith("$")) {
            return tokenName;
        }
        String escapedName = tokenName
                .replace("\\", "\\\\")
                .replace("'", "\\'");
        return "$['" + escapedName + "']";
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

    private String doRequest(final HttpEntity<String> request) {
        RestClient restClient = RestClient.create();
        try {
            return restClient.post()
                    .uri(configuration.getLoginEndpoint())
                    .headers(h -> h.addAll(request.getHeaders()))
                    .body(request.getBody())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            logResponseStatusError(exception);
            throw new DefaultFrameworkException(
                    "JWT login failed for auth strategy <" + configuration.getAlias() + ">", exception);
        }
    }

    private String getCredentialsFromFile(final String fileName) {
        try {
            String content = FileUtils.readFileToString(fileSearcher.searchFileFromDataFolder(fileName),
                    StandardCharsets.UTF_8);
            return systemVariableService.inject(content);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, configuration.getAlias());
        log.info(ENDPOINT_LOG, configuration.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }

    private void logResponseStatusError(final RestClientResponseException exception) {
        log.info(SERVER_ERROR_RESPONSE_LOG, exception.getStatusCode().value());
    }
}
