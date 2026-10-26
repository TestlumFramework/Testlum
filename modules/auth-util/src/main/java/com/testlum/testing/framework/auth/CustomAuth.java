package com.testlum.testing.framework.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.testlum.log.LogFormat;
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
public class CustomAuth extends AbstractAuthStrategy {

    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String ENDPOINT_LOG = LogFormat.table("Endpoint");
    private static final String CREDENTIALS_LOG = LogFormat.table("Credentials");

    private static final String AUTHENTICATION_TYPE = "Authentication type";
    private static final String CUSTOM_PREFIX = "Custom ";

    private final FileSearcher fileSearcher;
    private final SystemVariableService systemVariableService;
    private final RestTemplate restTemplate;
    private final CustomAuthStrategy configuration;

    public CustomAuth(final InterpreterDependencies dependencies,
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
        result.put(AUTHENTICATION_TYPE, "CUSTOM");
        login(token, CUSTOM_PREFIX);
    }

    private String getCustomToken(final Auth auth) {
        final String body = getCredentialsFromFile(auth.getCredentials());
        final HttpEntity<String> request = new HttpEntity<>(body, getHeaders());
        final String response = restTemplate.postForObject(getLoginEndpoint(), request, String.class);
        if (StringUtils.isBlank(response)) {
            throw new DefaultFrameworkException(
                    "Login response is empty for auth strategy <%s>", configuration.getAlias());
        }
        return extractToken(response);
    }

    private String extractToken(final String response) {
        final DocumentContext context = JsonPath.parse(response);
        final Object value = context.read("$.token");
        if (!(value instanceof String token) || StringUtils.isBlank(token)) {
            throw new DefaultFrameworkException("Login response must contain a non-empty customToken "
                    + "for auth strategy <%s>", configuration.getAlias());
        }
        return token;
    }

    private HttpHeaders getHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }

    private String getLoginEndpoint() {
        final String endpoint = configuration.getLoginEndpoint();
        if (StringUtils.isBlank(endpoint)) {
            throw new DefaultFrameworkException(
                    "Login endpoint is required by CustomAuth for strategy <%s>", configuration.getAlias());
        }
        return endpoint;
    }

    @SneakyThrows
    private String getCredentialsFromFile(final String fileName) {
        String content = FileUtils.readFileToString(fileSearcher.searchFileFromDataFolder(fileName),
                StandardCharsets.UTF_8);
        return systemVariableService.inject(content);
    }

    private void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, configuration.getAlias());
        log.info(ENDPOINT_LOG, configuration.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }
}