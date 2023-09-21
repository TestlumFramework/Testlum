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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class JwtAuth extends AbstractAuthStrategy {

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
        LogUtil.logAuthInfo(auth);
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
            LogUtil.logResponseStatusError(exception);
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
}
