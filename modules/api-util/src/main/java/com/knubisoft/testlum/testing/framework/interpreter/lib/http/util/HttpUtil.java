package com.knubisoft.testlum.testing.framework.interpreter.lib.http.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.google.common.base.Ascii;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;

@Component
@RequiredArgsConstructor
public final class HttpUtil {

    public static final String INCORRECT_HTTP_PROCESSING = "Incorrect http processing";
    public static final String UNKNOWN_BODY_CONTENT = "Unknown body content provided";
    private static final String ABSENT_PARAMETER_VARIATION_KEYWORD = "$absent";

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

    private final FileSearcher fileSearcher;
    private final JacksonService jacksonService;

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

    public HttpEntity extractBody(final Body body,
                                  final ContentType contentType,
                                  final AbstractInterpreter<?> interpreter,
                                  final InterpreterDependencies dependencies) {
        try {
            return getAppropriateEntity(body, contentType, interpreter, dependencies);
        } catch (Exception e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private HttpEntity getAppropriateEntity(final Body body,
                                            final ContentType contentType,
                                            final AbstractInterpreter<?> interpreter,
                                            final InterpreterDependencies dependencies) {
        if (Objects.isNull(body)) {
            return newStringEntity(StringUtils.EMPTY, null);
        } else if (Objects.nonNull(body.getRaw())) {
            return getFromRaw(body, contentType);
        } else if (Objects.nonNull(body.getFrom())) {
            return getFromFile(body, contentType, interpreter);
        } else if (Objects.nonNull(body.getMultipart())) {
            return getFromMultipart(body, contentType, dependencies);
        } else if (!body.getParam().isEmpty()) {
            return getFromParameters(body, contentType);
        }
        throw new DefaultFrameworkException(UNKNOWN_BODY_CONTENT);
    }

    private HttpEntity newStringEntity(final String body, final ContentType contentType) {
        return new StringEntity(body, contentType);
    }

    private HttpEntity getFromRaw(final Body body, final ContentType contentType) {
        String content = body.getRaw().replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE).trim();
        return newStringEntity(content, contentType);
    }

    private HttpEntity getFromFile(final Body body,
                                   final ContentType contentType,
                                   final AbstractInterpreter<?> interpreter) {
        String fileName = body.getFrom().getFile();
        String content = interpreter.getContentIfFile(fileName);
        return newStringEntity(content, contentType);
    }

    private HttpEntity getFromMultipart(final Body body,
                                        final ContentType contentType,
                                        final InterpreterDependencies dependencies) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (ContentType.MULTIPART_FORM_DATA.getMimeType().equalsIgnoreCase(contentType.getMimeType())) {
            builder.setContentType(contentType);
        }
        for (Object part : body.getMultipart().getParamOrFile()) {
            if (part instanceof PartParam partParam) {
                addTextBody(builder, partParam);
            } else if (part instanceof PartFile partFile) {
                addFileBody(builder, partFile, dependencies.getFile());
            }
        }
        return builder.build();
    }

    private void addTextBody(final MultipartEntityBuilder builder, final PartParam param) {
        builder.addTextBody(param.getName(), param.getData(), StringUtils.isNotBlank(param.getContentType())
                ? ContentType.parse(param.getContentType()) : ContentType.DEFAULT_TEXT);
    }

    private void addFileBody(final MultipartEntityBuilder builder,
                             final PartFile file,
                             final File fromDir) {
        File from = fileSearcher.searchFileFromDir(fromDir, file.getFileName());
        builder.addBinaryBody(file.getName(), from, StringUtils.isNotBlank(file.getContentType())
                ? ContentType.parse(file.getContentType()) : ContentType.DEFAULT_BINARY, file.getFileName());
    }

    private HttpEntity getFromParameters(final Body body, final ContentType contentType) {
        Map<String, String> bodyParamMap = body.getParam().stream()
                .collect(Collectors.toMap(Param::getName, Param::getData, (k, v) -> k, LinkedHashMap::new));

        if (ContentType.APPLICATION_JSON.getMimeType().equalsIgnoreCase(contentType.getMimeType())) {
            String params = jacksonService.writeValueAsString(bodyParamMap);
            return newStringEntity(params, contentType);
        }
        List<BasicNameValuePair> paramList = bodyParamMap.entrySet().stream()
                .map(e -> new BasicNameValuePair(e.getKey(), e.getValue()))
                .toList();
        return new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
    }

    public void fillHeadersMap(final List<com.knubisoft.testlum.testing.model.scenario.Header> headerList,
                               final Map<String, String> headers,
                               final InterpreterDependencies.Authorization authorization) {
        if (Objects.nonNull(authorization) && !authorization.getHeaders().isEmpty()) {
            headers.putAll(authorization.getHeaders());
        }
        for (com.knubisoft.testlum.testing.model.scenario.Header header : headerList) {
            headers.put(header.getName(), header.getData());
        }
    }

    public ContentType computeContentType(final Map<String, String> headers) {
        String typeValue = headers.computeIfAbsent(HttpHeaders.CONTENT_TYPE, type -> MediaType.APPLICATION_JSON_VALUE);
        return ContentType.parse(typeValue);
    }

    public String sanitizeEndpointForAbsentKeywordsIfPresent(final String endpoint) {
        if (!endpoint.contains("?")) {
            return endpoint;
        }
        String[] endpointUrlDividedByQuotationMark = endpoint.split("\\?");
        String urlPartBeforeQuotationMark = endpointUrlDividedByQuotationMark[0];
        String urlPartAfterQuotationMark = endpointUrlDividedByQuotationMark[1];
        String[] queryParametersPairs = urlPartAfterQuotationMark.split("&");
        String sanitizedQueryParamsString = Arrays.stream(queryParametersPairs)
                .filter(s -> !s.contains(ABSENT_PARAMETER_VARIATION_KEYWORD))
                .collect(Collectors.joining("&"));
        if (sanitizedQueryParamsString.isEmpty()) {
            return endpoint.substring(0, endpoint.indexOf("?"));
        }
        return urlPartBeforeQuotationMark.concat("?").concat(sanitizedQueryParamsString);
    }

    public List<com.knubisoft.testlum.testing.model.scenario.Header> sanitizeHeadersForAbsentKeyword(final HttpInfo httpInfo) {
        return httpInfo.getHeader().stream()
                .filter(header -> !header.getData().trim().equals(ABSENT_PARAMETER_VARIATION_KEYWORD))
                .toList();
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
