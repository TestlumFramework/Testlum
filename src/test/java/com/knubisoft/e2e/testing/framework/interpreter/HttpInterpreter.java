package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.e2e.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.model.scenario.Header;
import com.knubisoft.e2e.testing.model.scenario.Http;
import com.knubisoft.e2e.testing.model.scenario.HttpInfoWithBody;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.HttpUtil;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import com.knubisoft.e2e.testing.model.scenario.Body;
import com.knubisoft.e2e.testing.model.scenario.HttpInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ERROR_LOG;

@Slf4j
@InterpreterForClass(Http.class)
public class HttpInterpreter extends AbstractInterpreter<Http> {

    private static final String HTTP_PREFIX = "http";
    private static final String FULL_URL_TEMPLATE = "%s%s";

    @Autowired
    private ApiClient apiClient;

    public HttpInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    //CHECKSTYLE:OFF
    @Override
    protected void acceptImpl(final Http http, final CommandResult result) {
        HttpUtil.HttpMethodMetadata metadata = HttpUtil.getHttpMethodMetadata(http);
        HttpInfo httpInfo = metadata.getHttpInfo();
        HttpMethod httpMethod = metadata.getHttpMethod();
        String url = inject(httpInfo.getUrl());
        result.put("url", url);
        result.put("method", httpMethod.name());
        ApiResponse actual = getActual(httpInfo, url, httpMethod, http.getAlias());
        result.setActual(PrettifyStringJson.getJSONResult(actual.getBody().toString()));
        result.put("actual_code", actual.getCode());
        CompareBuilder compare = newCompare()
                .withActual(actual)
                .withExpectedFile(httpInfo.getResponse().getFile());
        compare.exec();
        setContextBody(actual.getBody().toString());
    }

    protected ApiResponse getActual(final HttpInfo httpInfo,
                                    final String url,
                                    final HttpMethod httpMethod,
                                    final String alias) {
        LogUtil.logHttpInfo(alias, httpMethod.name(), url,
                dependencies.getGlobalTestConfiguration().getIntegrations().getApis().getApi()
                        .stream().filter(a -> a.getAlias().equalsIgnoreCase(alias))
                        .findFirst().get().getUrl());
        Map<String, String> headers = getHeaders(httpInfo);
        boolean isJson = headers.getOrDefault(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE);
        HttpEntity body = getBody(httpInfo, isJson);
        LogUtil.logBodyContent(body);
        try {
            return apiClient.call(httpMethod, url, headers, body, alias);
        } catch (IOException e) {
            log.error(ERROR_LOG, e);
            throw new DefaultFrameworkException(e);
        }
    }
    //CHECKSTYLE:ON

    private Map<String, String> getHeaders(final HttpInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        fillHeadersMap(httpInfo, headers, authorization);
        return HttpUtil.injectAndGetHeaders(headers, this);
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

    private HttpEntity getBody(final HttpInfo httpInfo, final boolean isJson) {
        if (!(httpInfo instanceof HttpInfoWithBody)) {
            return null;
        }
        HttpInfoWithBody commandWithBody = (HttpInfoWithBody) httpInfo;
        Body body = commandWithBody.getBody();
        return HttpUtil.extractBody(body, isJson, this, dependencies);
    }
}
