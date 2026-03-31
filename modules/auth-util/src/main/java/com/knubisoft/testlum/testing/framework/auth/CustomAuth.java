package com.knubisoft.testlum.testing.framework.auth;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Auth;
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
import java.util.List;

@Slf4j
public class CustomAuth extends AbstractAuthStrategy {

    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String ENDPOINT_LOG = LogFormat.table("Endpoint");
    private static final String CREDENTIALS_LOG = LogFormat.table("Credentials");

    private static final String AUTHENTICATION_TYPE = "Authentication type";
    private static final String CUSTOM_PREFIX = "Custom ";

    private final IntegrationsProvider integrationsProvider;
    private final List<Api> apiList;
    private final FileSearcher fileSearcher;

    public CustomAuth(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
        this.apiList = integrationsProvider.findListByEnv(Api.class, dependencies.getEnvironment());
        this.fileSearcher = dependencies.getContext().getBean(FileSearcher.class);
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

        final String response = new RestTemplate().postForObject(getFullApiUrl(auth), request, String.class);
        if (StringUtils.isBlank(response)) {
            return DelimiterConstant.EMPTY;
        }

        return extractToken(auth, response);
    }

    private String extractToken(final Auth auth, final String response) {
        final DocumentContext context = JsonPath.parse(response);
        final Api apiIntegration = integrationsProvider.findApiForAlias(apiList, auth.getApiAlias());

        final String tokenName = StringUtils.isNotBlank(apiIntegration.getAuth().getTokenName())
                ? apiIntegration.getAuth().getTokenName()
                : "customToken";

        return context.read("$." + tokenName);
    }

    private HttpHeaders getHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }

    private String getFullApiUrl(final Auth auth) {
        final Api apiIntegration = integrationsProvider.findApiForAlias(apiList, auth.getApiAlias());
        return apiIntegration.getUrl() + auth.getLoginEndpoint();
    }

    @SneakyThrows
    private String getCredentialsFromFile(final String fileName) {
        return FileUtils.readFileToString(
                fileSearcher.searchFileFromDataFolder(fileName),
                StandardCharsets.UTF_8
        );
    }

    private void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, auth.getApiAlias());
        log.info(ENDPOINT_LOG, auth.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }
}