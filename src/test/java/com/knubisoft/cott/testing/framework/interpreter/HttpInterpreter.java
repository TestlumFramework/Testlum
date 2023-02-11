package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.cott.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.HttpUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Body;
import com.knubisoft.cott.testing.model.scenario.Http;
import com.knubisoft.cott.testing.model.scenario.HttpInfo;
import com.knubisoft.cott.testing.model.scenario.HttpInfoWithBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@InterpreterForClass(Http.class)
public class HttpInterpreter extends AbstractInterpreter<Http> {

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
        ResultUtil.addHttpMetaData(http.getAlias(), httpInfo, httpMethod.name(), result);
        String endpoint = inject(httpInfo.getEndpoint());
        ApiResponse actual = getActual(httpInfo, endpoint, httpMethod, http.getAlias());
        CompareBuilder compare = newCompare()
                .withActual(actual)
                .withExpectedFile(httpInfo.getResponse().getFile());
        result.setActual(PrettifyStringJson.getJSONResult(toString(actual)));
        result.setExpected(PrettifyStringJson.getJSONResult(compare.getExpected()));
        compare.exec();
        setContextBody(actual.getBody().toString());
    }

    protected ApiResponse getActual(final HttpInfo httpInfo,
                                    final String endpoint,
                                    final HttpMethod httpMethod,
                                    final String alias) {
        LogUtil.logHttpInfo(alias, httpMethod.name(), endpoint);
        Map<String, String> headers = getHeaders(httpInfo);
        String typeValue = headers.getOrDefault(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ContentType contentType = ContentType.create(typeValue);
        HttpEntity body = getBody(httpInfo, contentType);
        LogUtil.logBodyContent(body);
        try {
            return apiClient.call(httpMethod, endpoint, headers, body, alias);
        } catch (IOException e) {
            LogUtil.logError(e);
            throw new DefaultFrameworkException(e);
        }
    }
    //CHECKSTYLE:ON

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
}
