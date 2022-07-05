package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.interpreter.AuthInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.e2e.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.e2e.testing.framework.util.HttpUtil;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import com.knubisoft.e2e.testing.model.scenario.Get;
import com.knubisoft.e2e.testing.model.scenario.Header;
import com.knubisoft.e2e.testing.model.scenario.Http;
import com.knubisoft.e2e.testing.model.scenario.HttpInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.HEADER_AUTHORIZATION;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.HEADER_BASIC;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CREDENTIALS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ERROR_LOG;


@Slf4j
public class BasicAuth implements AuthStrategy {
    private static final int STATUS_CODE = 200;
    @Autowired
    private ApiClient apiClient;


    @Override
    public void login(final InterpreterDependencies dependencies, final Auth auth) {
        String url = dependencies.getGlobalTestConfiguration().getAuth().getBasic().getUrl();
        String credentials = encodedCredentials(auth, dependencies);
        ApiResponse response = sendGetRequest(credentials, url, auth.getAlias(), dependencies);
        if (response.getCode() != STATUS_CODE) {
            throw new DefaultFrameworkException(response.getBody().toString());
        }
    }

    @SneakyThrows
    private String encodedCredentials(final Auth auth, final InterpreterDependencies dependencies) {
        String credentials = getCredentialsFromFile(auth, dependencies);
        final ObjectNode node = new ObjectMapper().readValue(credentials, ObjectNode.class);
        credentials = (node.get("username") + ":" + node.get("password")).replaceAll("\"", "");
        log.info(CREDENTIALS_LOG, credentials);
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String getCredentialsFromFile(final Auth auth, final InterpreterDependencies dependencies) {
        File credentialsFolder = TestResourceSettings.getInstance().getCredentialsFolder();
        return dependencies.getFileSearcher()
                .searchFileAndReadToString(credentialsFolder, auth.getCredentials());
    }

    private ApiResponse sendGetRequest(final String credentials, final String url, final String alias,
                                        final InterpreterDependencies dependencies) {
        final Http http = createHttpGet();
        final HttpUtil.HttpMethodMetadata metadata = HttpUtil.getHttpMethodMetadata(http);
        final HttpInfo httpInfo = metadata.getHttpInfo();
        final HttpMethod httpMethod = metadata.getHttpMethod();
        setAuthHeaders(dependencies, credentials);
        return getActual(httpInfo, url, httpMethod, alias, dependencies);
    }

    @NotNull
    private Http createHttpGet() {
        final Http http = new Http();
        final Get get = new Get();
        http.setGet(get);
        return http;
    }

    private void setAuthHeaders(final InterpreterDependencies dependencies, final String credentials) {
        final InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        final Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_AUTHORIZATION, HEADER_BASIC + credentials);
        authorization.setHeaders(headers);
        dependencies.setAuthorization(authorization);
    }


    protected ApiResponse getActual(final HttpInfo httpInfo,
                                    final String url,
                                    final HttpMethod httpMethod,
                                    final String alias, final InterpreterDependencies dependencies) {
        LogUtil.logHttpInfo(alias, httpMethod.name(), url);
        Map<String, String> headers = getHeaders(httpInfo, dependencies);
        try {
            return apiClient.call(httpMethod, url, headers, null, alias);
        } catch (IOException e) {
            log.error(ERROR_LOG, e);
            throw new DefaultFrameworkException(e);
        }
    }

    private Map<String, String> getHeaders(final HttpInfo httpInfo, final InterpreterDependencies dependencies) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        fillHeadersMap(httpInfo, headers, authorization);
        return HttpUtil.injectAndGetHeaders(headers, new AuthInterpreter(dependencies));
    }

    private void fillHeadersMap(final HttpInfo httpInfo,
                                final Map<String, String> headers,
                                final InterpreterDependencies.Authorization authorization) {
        if (authorization != null && !authorization.getHeaders().isEmpty()) {
            headers.putAll(authorization.getHeaders());
        }
        for (Header header : httpInfo.getHeader()) {
            headers.put(header.getName(), header.getData());
        }
    }

}
