package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.util.HttpValidator;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.PrettifyStringJson;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Body;
import com.knubisoft.testlum.testing.model.scenario.Header;
import com.knubisoft.testlum.testing.model.scenario.Http;
import com.knubisoft.testlum.testing.model.scenario.HttpInfo;
import com.knubisoft.testlum.testing.model.scenario.HttpInfoWithBody;
import com.knubisoft.testlum.testing.model.scenario.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Http.class)
public class HttpInterpreter extends AbstractInterpreter<Http> {

    @Autowired
    private ApiClient apiClient;

    public HttpInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Http http, final CommandResult result) {
        HttpUtil.HttpMethodMetadata metadata = HttpUtil.getHttpMethodMetadata(http);
        HttpInfo httpInfo = metadata.getHttpInfo();
        HttpMethod httpMethod = metadata.getHttpMethod();
        ApiResponse actual = getActual(httpInfo, httpMethod, http.getAlias(), result);
        compareResult(httpInfo.getResponse(), actual, result);
    }

    private void compareResult(final Response expected,
                               final ApiResponse actual,
                               final CommandResult result) {
        HttpValidator httpValidator = new HttpValidator(this);
        httpValidator.validateCode(expected.getCode(), actual.getCode());
        String actualBody = String.valueOf(actual.getBody());
        setContextBody(actualBody);
        validateBody(expected, actualBody, httpValidator, result);
        validateHeaders(expected, actual, httpValidator);
        httpValidator.rethrowOnErrors();
    }

    private void validateBody(final Response expected,
                              final String actualBody,
                              final HttpValidator httpValidator,
                              final CommandResult result) {
        String body = StringUtils.isBlank(expected.getFile())
                ? DelimiterConstant.EMPTY
                : FileSearcher.searchFileToString(expected.getFile(), dependencies.getFile());
        result.setActual(PrettifyStringJson.getJSONResult(actualBody));
        result.setExpected(PrettifyStringJson.getJSONResult(body));
        httpValidator.validateBody(body, actualBody);
    }

    private void validateHeaders(final Response expected,
                                 final ApiResponse actual,
                                 final HttpValidator httpValidator) {
        if (!expected.getHeader().isEmpty()) {
            httpValidator.validateHeaders(getExpectedHeaders(expected), actual.getHeaders());
        }
    }

    private Map<String, String> getExpectedHeaders(final Response expected) {
        Map<String, String> expectedHeaders =
                expected.getHeader().stream().collect(Collectors.toMap(Header::getName, Header::getData));
        return HttpUtil.injectAndGetHeaders(expectedHeaders, this);
    }

    protected ApiResponse getActual(final HttpInfo httpInfo,
                                    final HttpMethod httpMethod,
                                    final String alias,
                                    final CommandResult result) {
        String endpoint = inject(httpInfo.getEndpoint());
        Map<String, String> headers = getHeaders(httpInfo);
        LogUtil.logHttpInfo(alias, httpMethod.name(), endpoint);
        ResultUtil.addHttpMetaData(alias, httpMethod.name(), headers, endpoint, result);
        ContentType contentType = HttpUtil.computeContentType(headers);
        HttpEntity body = getBody(httpInfo, contentType);
        mergeContentTypeFromBody(headers, body);
        LogUtil.logBodyContent(body);
        String url = createFullUrl(endpoint, alias);
        return getApiResponse(httpMethod, url, headers, body);
    }

    private void mergeContentTypeFromBody(final Map<String, String> headers, final HttpEntity body) {
        if (nonNull(body) && nonNull(body.getContentType())) {
            String contentType = body.getContentType().getValue();
            headers.merge(HttpHeaders.CONTENT_TYPE, contentType, (k, v) -> contentType.equalsIgnoreCase(v)
                    ? v : contentType);
        }
    }

    private ApiResponse getApiResponse(final HttpMethod httpMethod,
                                       final String url,
                                       final Map<String, String> headers,
                                       final HttpEntity body) {
        try {
            return apiClient.call(httpMethod, url, headers, body);
        } catch (Exception e) {
            LogUtil.logError(e);
            throw new DefaultFrameworkException(e);
        }
    }

    private Map<String, String> getHeaders(final HttpInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        HttpUtil.fillHeadersMap(httpInfo.getHeader(), headers, authorization);
        return HttpUtil.injectAndGetHeaders(headers, this);
    }

    private HttpEntity getBody(final HttpInfo httpInfo, final ContentType contentType) {
        if (!(httpInfo instanceof HttpInfoWithBody)) {
            return null;
        }
        HttpInfoWithBody commandWithBody = (HttpInfoWithBody) httpInfo;
        Body body = commandWithBody.getBody();
        return HttpUtil.extractBody(body, contentType, this, dependencies);
    }

    private String createFullUrl(final String endpoint, final String alias) {
        List<Api> apiList = GlobalTestConfigurationProvider.getIntegrations().get(dependencies.getEnvironment())
                .getApis().getApi();
        Api apiIntegration = IntegrationsUtil.findApiForAlias(apiList, alias);
        return apiIntegration.getUrl() + endpoint;
    }
}
