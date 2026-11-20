package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.SystemVariableService;
import com.testlum.testing.model.global_config.CustomAuthStrategy;
import com.testlum.testing.model.scenario.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
public class CustomAuthLocal extends AbstractAuthStrategy {

    private static final String LOGIN_ENDPOINT = "http://localhost:8080/api/v1/api/test/auth/custom/login";

    private final FileSearcher fileSearcher;
    private final SystemVariableService systemVariableService;
    private final RestTemplate restTemplate;
    private final CustomAuthStrategy configuration;

    public CustomAuthLocal(final InterpreterDependencies dependencies,
                           final CustomAuthStrategy configuration) {
        super(dependencies);
        this.configuration = configuration;
        this.fileSearcher = dependencies.getContext().getBean(FileSearcher.class);
        this.systemVariableService = dependencies.getContext().getBean(SystemVariableService.class);
        this.restTemplate = dependencies.getContext().getBean(RestTemplate.class);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        logAuthInfo(auth);
        final String token = getCustomToken(auth);
        result.put(AUTHENTICATION_TYPE, AuthorizationConstant.CUSTOM_AUTHENTICATION_TYPE);
        login(token, AuthorizationConstant.HEADER_CUSTOM);
    }

    private String getCustomToken(final Auth auth) {
        final String body = getCredentialsFromFile(auth.getCredentials());
        final HttpEntity<String> request = new HttpEntity<>(body, getHeaders());
        final String response = restTemplate.postForObject(LOGIN_ENDPOINT, request, String.class);
        if (StringUtils.isBlank(response)) {
            throw new DefaultFrameworkException(
                    ExceptionMessage.AUTH_LOGIN_RESPONSE_EMPTY, configuration.getAlias());
        }
        return extractToken(response);
    }

    private String extractToken(final String response) {
        final String path = DelimiterConstant.DOLLAR_SIGN + DelimiterConstant.DOT
                + AuthorizationConstant.CONTENT_KEY_TOKEN;
        final Object value = readTokenValue(response, path);
        if (!(value instanceof String token) || StringUtils.isBlank(token)) {
            throw new DefaultFrameworkException(
                    ExceptionMessage.AUTH_CUSTOM_LOGIN_TOKEN_INVALID, configuration.getAlias());
        }
        return token;
    }

    private Object readTokenValue(final String response, final String path) {
        try {
            return JsonPath.parse(response).read(path);
        } catch (PathNotFoundException exception) {
            throw new DefaultFrameworkException(ExceptionMessage.AUTH_LOGIN_TOKEN_NOT_FOUND,
                    path, configuration.getAlias());
        }
    }

    private HttpHeaders getHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }

    @SneakyThrows
    private String getCredentialsFromFile(final String fileName) {
        String content = FileUtils.readFileToString(fileSearcher.searchFileFromDataFolder(fileName),
                StandardCharsets.UTF_8);
        return systemVariableService.inject(content);
    }

    private void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, configuration.getAlias());
        log.info(ENDPOINT_LOG, LOGIN_ENDPOINT);
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }
}
