package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.model.scenario.Body;
import com.knubisoft.cott.testing.model.scenario.ElasticSearchRequest;
import com.knubisoft.cott.testing.model.scenario.Elasticsearch;
import com.knubisoft.cott.testing.model.scenario.Http;
import com.knubisoft.cott.testing.model.scenario.HttpInfo;
import com.knubisoft.cott.testing.model.scenario.Multipart;
import com.knubisoft.cott.testing.model.scenario.Param;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INCORRECT_HTTP_PROCESSING;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

@UtilityClass
public final class HttpUtil {

    private static final String EQUALS_BETWEEN_VALUES = "%s=%s";
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
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getPatch, HttpMethod.PATCH);
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getDelete, HttpMethod.DELETE);
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getHead, HttpMethod.HEAD);
        ES_HTTP_METHOD_MAP.put(Elasticsearch::getTrace, HttpMethod.TRACE);
    }

    public HttpEntity extractBody(final Body body,
                                  final ContentType contentType,
                                  final AbstractInterpreter<?> interpreter,
                                  final InterpreterDependencies dependencies) {
        try {
            return injectAppropriatePart(body, contentType, interpreter, dependencies);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public HttpMethodMetadata getHttpMethodMetadata(final Http http) {
        return HTTP_METHOD_MAP.entrySet().stream()
                .map(e -> new HttpMethodMetadata(e.getKey().apply(http), e.getValue()))
                .filter(p -> Objects.nonNull(p.getHttpInfo()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    public ESHttpMethodMetadata getESHttpMethodMetadata(final Elasticsearch elasticsearch) {
        return ES_HTTP_METHOD_MAP.entrySet().stream()
                .map(e -> new ESHttpMethodMetadata(e.getKey().apply(elasticsearch), e.getValue()))
                .filter(p -> Objects.nonNull(p.getElasticSearchRequest()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    public boolean checkIfContentTypeIsJson(final Header contentTypeHeader) {
        if (contentTypeHeader != null) {
            return contentTypeHeader.getValue().equals(MediaType.APPLICATION_JSON_VALUE)
                    || contentTypeHeader.getValue().equals(MediaType.APPLICATION_JSON_UTF8_VALUE);
        }
        return false;
    }

    //CHECKSTYLE:OFF
    private HttpEntity injectAppropriatePart(final Body body,
                                             final ContentType contentType,
                                             final AbstractInterpreter<?> interpreter,
                                             final InterpreterDependencies dependencies) throws IOException {
        if (body == null) {
            return getStringEntity(StringUtils.EMPTY, contentType);
        } else if (body.getRaw() != null) {
            String injected = interpreter.inject(body.getRaw());
            return getStringEntity(injected, contentType);
        } else if (body.getFrom() != null) {
            return injectFromFile(body, interpreter, dependencies);
        } else if (body.getMultipart() != null) {
            return injectMultipartFile(body, dependencies);
        }
        String param = getFromParam(contentType, body, interpreter);
        String injected = interpreter.inject(param);
        return getStringEntity(injected, contentType);
    }
    //CHECKSTYLE:ON

    private HttpEntity getStringEntity(final String body, final ContentType contentType) {
        return new StringEntity(body, contentType);
    }

    private HttpEntity injectMultipartFile(final Body body,
                                           final InterpreterDependencies dependencies) {
        Multipart multipart = body.getMultipart();
        File from = FileSearcher.searchFileFromDir(dependencies.getFile(), multipart.getPath());

        return MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addPart(multipart.getName(), new FileBody(from))
                .build();
    }

    private HttpEntity injectFromFile(final Body body,
                                      final AbstractInterpreter<?> interpreter,
                                      final InterpreterDependencies dependencies) throws IOException {
        String injectedContent = injectFromFile(body, interpreter, dependencies.getFile());
        return new StringEntity(injectedContent, ContentType.APPLICATION_JSON);
    }

    public String injectFromFile(final Body body,
                                 final AbstractInterpreter<?> interpreter,
                                 final File fromDir) throws IOException {
        String fileName = body.getFrom().getFile();
        File from = FileSearcher.searchFileFromDir(fromDir, fileName);
        String content = FileUtils.readFileToString(from, StandardCharsets.UTF_8);
        return interpreter.inject(content);
    }

    public void fillHeadersMap(final List<com.knubisoft.cott.testing.model.scenario.Header> headerList,
                               final Map<String, String> headers,
                               final InterpreterDependencies.Authorization authorization) {
        if (authorization != null && !authorization.getHeaders().isEmpty()) {
            headers.putAll(authorization.getHeaders());
        }
        for (com.knubisoft.cott.testing.model.scenario.Header header : headerList) {
            headers.put(header.getName(), header.getData());
        }
    }

    public Map<String, String> injectAndGetHeaders(final Map<String, String> headersMap,
                                                   final AbstractInterpreter<?> interpreter) {
        Map<String, String> injected = new LinkedHashMap<>(headersMap.size());
        for (Map.Entry<String, String> each : headersMap.entrySet()) {
            injected.put(interpreter.inject(each.getKey()), interpreter.inject(each.getValue()));
        }
        return injected;
    }

    private String getFromParam(final ContentType contentType,
                                final Body body,
                                final AbstractInterpreter<?> interpreter) {
        Map<String, String> bodyParamMap = body.getParam().stream()
                .collect(toMap(Param::getName, Param::getData, (k, v) -> k, LinkedHashMap::new));

        if (ContentType.APPLICATION_JSON == contentType) {
            return interpreter.toString(bodyParamMap);
        }
        return bodyParamMap.entrySet().stream()
                .map(e -> format(EQUALS_BETWEEN_VALUES, e.getKey(), e.getValue()))
                .collect(Collectors.joining(DelimiterConstant.AMPERSAND));
    }

    @RequiredArgsConstructor
    @Getter
    public static class HttpMethodMetadata {
        private final HttpInfo httpInfo;
        private final HttpMethod httpMethod;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ESHttpMethodMetadata {
        private final ElasticSearchRequest elasticSearchRequest;
        private final HttpMethod httpMethod;
    }
}
