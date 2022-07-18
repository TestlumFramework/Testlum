package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.model.scenario.Param;
import com.knubisoft.e2e.testing.model.scenario.Sendgrid;
import com.knubisoft.e2e.testing.model.scenario.Body;
import com.knubisoft.e2e.testing.model.scenario.SendgridInfo;
import com.sendgrid.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.INCORRECT_HTTP_PROCESSING;
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
                .filter(p -> Objects.nonNull(p.getHttpInfo()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    public String injectAppropriatePart(final Body body,
                                        final AbstractInterpreter<?> interpreter,
                                        final InterpreterDependencies dependencies) throws IOException {
        if (body == null) {
            return StringUtils.EMPTY;
        } else if (body.getRaw() != null) {
            return interpreter.inject(body.getRaw());
        } else if (body.getFrom() != null) {
            return injectFromFile(body, interpreter, dependencies);
        }
        return interpreter.inject(getFromParam(body, interpreter));
    }

    private String injectFromFile(final Body body,
                                  final AbstractInterpreter<?> interpreter,
                                  final InterpreterDependencies dependencies) throws IOException {
        String fileName = body.getFrom().getFile();
        File from = FileSearcher.searchFileFromDir(dependencies.getFile(), fileName);
        String content = FileUtils.readFileToString(from, StandardCharsets.UTF_8);
        return interpreter.inject(content);
    }

    public Map<String, String> injectAndGetHeaders(final Map<String, String> headersMap,
                                                   final AbstractInterpreter<?> interpreter) {
        Map<String, String> injected = new LinkedHashMap<>(headersMap.size());
        for (Map.Entry<String, String> each : headersMap.entrySet()) {
            injected.put(interpreter.inject(each.getKey()), interpreter.inject(each.getValue()));
        }
        return injected;
    }

    private String getFromParam(final Body body,
                                final AbstractInterpreter<?> interpreter) {
        Map<String, String> bodyParamMap = body.getParam().stream()
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
