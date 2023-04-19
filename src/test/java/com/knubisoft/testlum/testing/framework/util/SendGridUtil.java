package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.scenario.Body;
import com.knubisoft.testlum.testing.model.scenario.Header;
import com.knubisoft.testlum.testing.model.scenario.Param;
import com.knubisoft.testlum.testing.model.scenario.Sendgrid;
import com.knubisoft.testlum.testing.model.scenario.SendgridInfo;
import com.sendgrid.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_HTTP_PROCESSING;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

@UtilityClass
public final class SendGridUtil {

    private static final Map<Function<Sendgrid, SendgridInfo>, Method> HTTP_METHOD_MAP =
            new HashMap<>(8);

    static {
        HTTP_METHOD_MAP.put(Sendgrid::getGet, Method.GET);
        HTTP_METHOD_MAP.put(Sendgrid::getPost, Method.POST);
        HTTP_METHOD_MAP.put(Sendgrid::getPut, Method.PUT);
        HTTP_METHOD_MAP.put(Sendgrid::getPatch, Method.PATCH);
        HTTP_METHOD_MAP.put(Sendgrid::getDelete, Method.DELETE);
    }

    public SendGridMethodMetadata getSendgridMethodMetadata(final Sendgrid sendgrid) {
        return HTTP_METHOD_MAP.entrySet().stream()
                .map(e -> new SendGridMethodMetadata(e.getKey().apply(sendgrid), e.getValue()))
                .filter(p -> nonNull(p.getHttpInfo()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    private String injectAppropriatePart(final Body body,
                                         final AbstractInterpreter<?> interpreter,
                                         final InterpreterDependencies dependencies) throws IOException {
        if (isNull(body)) {
            return StringUtils.EMPTY;
        } else if (nonNull(body.getRaw())) {
            return interpreter.inject(body.getRaw());
        } else if (nonNull(body.getFrom())) {
            return injectFromFile(body, interpreter, dependencies);
        }
        return interpreter.inject(getFromParam(body, interpreter));
    }

    private String injectFromFile(final Body body,
                                  final AbstractInterpreter<?> interpreter,
                                  final InterpreterDependencies dependencies) throws IOException {
        return HttpUtil.injectFromFile(body, interpreter, dependencies.getFile());
    }

    public Map<String, String> injectAndGetHeaders(final Map<String, String> headersMap,
                                                   final AbstractInterpreter<?> interpreter) {
        return HttpUtil.injectAndGetHeaders(headersMap, interpreter);
    }

    public void fillHeadersMap(final List<Header> headerList,
                               final Map<String, String> headers,
                               final InterpreterDependencies.Authorization authorization) {
        HttpUtil.fillHeadersMap(headerList, headers, authorization);
    }

    private String getFromParam(final Body body,
                                final AbstractInterpreter<?> interpreter) {
        List<Param> params = body.getParam();
        Map<String, String> bodyParamMap = params.stream()
                .collect(toMap(Param::getName, Param::getData, (k, v) -> k, LinkedHashMap::new));

        return interpreter.toString(bodyParamMap);
    }

    public String extractBody(final Body body,
                              final AbstractInterpreter<?> interpreter,
                              final InterpreterDependencies dependencies) {
        try {
            return injectAppropriatePart(body, interpreter, dependencies);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class SendGridMethodMetadata {
        private final SendgridInfo httpInfo;
        private final Method httpMethod;
    }
}
