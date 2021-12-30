package com.knubisoft.e2e.testing.framework.interpreter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.e2e.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.e2e.testing.model.scenario.Header;
import com.knubisoft.e2e.testing.model.scenario.Http;
import com.knubisoft.e2e.testing.model.scenario.HttpInfoWithBody;
import com.knubisoft.e2e.testing.model.scenario.Post;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.HttpUtil;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import com.knubisoft.e2e.testing.model.scenario.Body;
import com.knubisoft.e2e.testing.model.scenario.HttpInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.CENTRAL_DB;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.CONTENT_KEY_TOKEN;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.EMAIL_JPATH;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.GET_TENANT_ID_QUERY;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.GET_TWO_FA_TOKEN_QUERY;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.HEADER_AUTHORIZATION;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.HEADER_BEARER;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.LOGIN_FIRST_STEP_URL;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.LOGIN_SECOND_STEP_JSON;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.LOGIN_SECOND_STEP_URL;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.TENANT_NAME;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.TOKEN_JPATH;
import static com.knubisoft.e2e.testing.framework.constant.AuthorizationConstant.TWO_FA_TOKEN_TYPE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ERROR_LOG;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Auth.class)
public class AuthInterpreter extends AbstractInterpreter<Auth> {
    private static final int STATUS_CODE = 200;
    @Autowired
    private ApiClient apiClient;
    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public AuthInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Auth auth, final CommandResult result) {
        final String firstStepToken = doAuthFirstStep(auth);
        final String credentials = getCredentialsFromFile(auth);
        final String email = getJpathValue(JsonPath.parse(credentials), EMAIL_JPATH);
        final String tenantId = (String) executeQueryAndGetResult(format(GET_TENANT_ID_QUERY, email),
                "tenant_id", CENTRAL_DB);
        final String twoFACode = (String) executeQueryAndGetResult(GET_TWO_FA_TOKEN_QUERY, CONTENT_KEY_TOKEN,
                format(TENANT_NAME, tenantId));
        createAuthTokenAndPutToHeaders(auth, result, firstStepToken, credentials, email, twoFACode);
    }

    private void createAuthTokenAndPutToHeaders(final Auth auth,
                                                final CommandResult result,
                                                final String firstStepToken,
                                                final String credentials,
                                                final String email,
                                                final String twoFACode) {
        final String authToken = doAuthSecondStep(email, firstStepToken, twoFACode, auth.getAlias());
        setAuthHeaders(authToken);
        result.put("credentials", credentials);
    }

    private String doAuthFirstStep(final Auth auth) {
        final String credentials = getCredentialsFromFile(auth);
        final ApiResponse response = sendPostRequest(credentials, LOGIN_FIRST_STEP_URL, auth.getAlias());
        if (response.getCode() != STATUS_CODE) {
            throw new DefaultFrameworkException(response.getBody());
        }
        return getJpathValue(JsonPath.parse(response.getBody()), TOKEN_JPATH);
    }

    private String getCredentialsFromFile(final Auth auth) {
        File credentialsFolder = TestResourceSettings.getInstance().getCredentialsFolder();
        return dependencies.getFileSearcher()
                .searchFileAndReadToString(credentialsFolder, auth.getCredentials());
    }

    private String doAuthSecondStep(final String email,
                                    final String firstStepToken,
                                    final String twoFACode,
                                    final String alias) {
        final String query = format(LOGIN_SECOND_STEP_JSON, email, firstStepToken,
                twoFACode, TWO_FA_TOKEN_TYPE);
        final ApiResponse response = sendPostRequest(query, LOGIN_SECOND_STEP_URL, alias);
        if (response.getCode() != STATUS_CODE) {
            throw new DefaultFrameworkException(response.getBody());
        }
        return getJpathValue(JsonPath.parse(response.getBody()), TOKEN_JPATH);
    }

    private void setAuthHeaders(final String authToken) {
        final InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        final Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_AUTHORIZATION, HEADER_BEARER + authToken);
        authorization.setHeaders(headers);
        dependencies.setAuthorization(authorization);
    }

    private Object executeQueryAndGetResult(final String query, final String contentKey, final String dbName) {
        final List<String> queryList = new ArrayList<>(Arrays.asList(query));
        final StorageOperation.StorageOperationResult queryResult = postgresSqlOperation
                .apply(new ListSource(queryList), dbName);
        final ArrayList<StorageOperation.QueryResult> raw =
                (ArrayList<StorageOperation.QueryResult>) queryResult.getRaw();
        final ArrayList<LinkedHashMap<String, String>> content =
                (ArrayList<LinkedHashMap<String, String>>) raw.get(0).getContent();
        return content.get(0).get(contentKey);
    }

    private String getJpathValue(final DocumentContext parse, final String jpath) {
        return parse.read(jpath);
    }

    private ApiResponse sendPostRequest(final String queryBody, final String url, final String alias) {
        final Http http = createHttpPost(queryBody);
        final HttpUtil.HttpMethodMetadata metadata = HttpUtil.getHttpMethodMetadata(http);
        final HttpInfo httpInfo = metadata.getHttpInfo();
        final HttpMethod httpMethod = metadata.getHttpMethod();
        return getActualResponse(httpInfo, url, httpMethod, alias);
    }

    @NotNull
    private Http createHttpPost(final String queryBody) {
        final Http http = new Http();
        final Post post = new Post();
        final Body body = new Body();
        body.setRaw(queryBody);
        post.setBody(body);
        http.setPost(post);
        return http;
    }

    private ApiResponse getActualResponse(final HttpInfo httpInfo, final String url,
                                          final HttpMethod httpMethod, final String alias) {
        Map<String, String> headers = getHeaders(httpInfo);
        boolean isJson = headers.getOrDefault(HttpHeaders.CONTENT_TYPE, StringUtils.EMPTY)
                .equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE);
        HttpEntity body = getBody(httpInfo, isJson);
        return doRequest(url, httpMethod, alias, headers, body);
    }

    private ApiResponse doRequest(final String url, final HttpMethod httpMethod,
                                  final String alias, final Map<String, String> headers,
                                  final HttpEntity body) {
        try {
            return apiClient.call(httpMethod, url, headers, body, alias);
        } catch (IOException e) {
            log.error(ERROR_LOG, e);
            throw new DefaultFrameworkException(e);
        }
    }

    private Map<String, String> getHeaders(final HttpInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        fillHeadersMap(httpInfo, headers, authorization);
        return HttpUtil.injectAndGetHeaders(headers, this);
    }

    private HttpEntity getBody(final HttpInfo httpInfo, final boolean isJson) {
        if (!(httpInfo instanceof HttpInfoWithBody)) {
            return null;
        }
        HttpInfoWithBody commandWithBody = (HttpInfoWithBody) httpInfo;
        Body body = commandWithBody.getBody();
        return HttpUtil.extractBody(body, isJson, this, dependencies);
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
