package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.ElasticSearchRequest;
import com.knubisoft.testlum.testing.model.scenario.Elasticsearch;
import com.knubisoft.testlum.testing.model.scenario.Http;
import com.knubisoft.testlum.testing.model.scenario.HttpInfo;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_HTTP_PROCESSING;
import static java.util.Objects.nonNull;

@UtilityClass
public final class HttpUtil {

    private static final Map<Function<Http, HttpInfo>, HttpMethod> HTTP_METHOD_MAP = new HashMap<>(8, 1F);
    private static final Map<Function<Elasticsearch, ElasticSearchRequest>, HttpMethod> ES_HTTP_METHOD_MAP =
            new HashMap<>(8);

    static {
        HTTP_METHOD_MAP.put(Http::getGet, HttpMethod.GET);
        HTTP_METHOD_MAP.put(Http::getPost, HttpMethod.POST);
        HTTP_METHOD_MAP.put(Http::getPut, HttpMethod.PUT);
        HTTP_METHOD_MAP.put(Http::getPatch, HttpMethod.PATCH);
        HTTP_METHOD_MAP.put(Http::getDelete, HttpMethod.DELETE);
        HTTP_METHOD_MAP.put(Http::getHead, HttpMethod.HEAD);
        HTTP_METHOD_MAP.put(Http::getOptions, HttpMethod.OPTIONS);
        HTTP_METHOD_MAP.put(Http::getTrace, HttpMethod.TRACE);

        ES_HTTP_METHOD_MAP.put(Elasticsearch::getGet, HttpMethod.GET);
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getPost, HttpMethod.POST);
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getPut, HttpMethod.PUT);
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getDelete, HttpMethod.DELETE);
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getHead, HttpMethod.HEAD);
    }

    public HttpMethodMetadata getHttpMethodMetadata(final Http http) {
        return HTTP_METHOD_MAP.entrySet().stream()
                .map(e -> new HttpMethodMetadata(e.getKey().apply(http), e.getValue()))
                .filter(p -> nonNull(p.httpInfo()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    public ESHttpMethodMetadata getESHttpMethodMetadata(final Elasticsearch elasticsearch) {
        return ES_HTTP_METHOD_MAP.entrySet().stream()
                .map(e -> new ESHttpMethodMetadata(e.getKey().apply(elasticsearch), e.getValue()))
                .filter(p -> nonNull(p.elasticSearchRequest()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    public record HttpMethodMetadata(HttpInfo httpInfo, HttpMethod httpMethod) {
    }

    public record ESHttpMethodMetadata(ElasticSearchRequest elasticSearchRequest, HttpMethod httpMethod) {
    }
}
