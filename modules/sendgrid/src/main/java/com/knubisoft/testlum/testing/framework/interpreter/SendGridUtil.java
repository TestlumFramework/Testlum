package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.model.scenario.Body;
import com.knubisoft.testlum.testing.model.scenario.Param;
import com.knubisoft.testlum.testing.model.scenario.Sendgrid;
import com.knubisoft.testlum.testing.model.scenario.SendgridInfo;
import com.sendgrid.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public final class SendGridUtil {

    private static final String INCORRECT_HTTP_PROCESSING = "Incorrect http processing";
    private static final Map<Function<Sendgrid, SendgridInfo>, Method> HTTP_METHOD_MAP =
            new HashMap<>(8);

    static {
        HTTP_METHOD_MAP.put(Sendgrid::getGet, Method.GET);
        HTTP_METHOD_MAP.put(Sendgrid::getPost, Method.POST);
        HTTP_METHOD_MAP.put(Sendgrid::getPut, Method.PUT);
        HTTP_METHOD_MAP.put(Sendgrid::getPatch, Method.PATCH);
        HTTP_METHOD_MAP.put(Sendgrid::getDelete, Method.DELETE);
    }

    private final JacksonService jacksonService;

    public SendGridMethodMetadata getSendgridMethodMetadata(final Sendgrid sendgrid) {
        return HTTP_METHOD_MAP.entrySet().stream()
                .map(e -> new SendGridMethodMetadata(e.getKey().apply(sendgrid), e.getValue()))
                .filter(p -> Objects.nonNull(p.getHttpInfo()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    public String extractBody(final Body body, final AbstractInterpreter<?> interpreter) {
        try {
            return getAppropriateBody(body, interpreter);
        } catch (Exception e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private String getAppropriateBody(final Body body, final AbstractInterpreter<?> interpreter) {
        if (Objects.isNull(body)) {
            return StringUtils.EMPTY;
        } else if (Objects.nonNull(body.getRaw())) {
            return getFromRaw(body);
        } else if (Objects.nonNull(body.getFrom())) {
            return getFromFile(body, interpreter);
        }
        return getFromParam(body);
    }

    private String getFromRaw(final Body body) {
        return body.getRaw();
    }

    private String getFromFile(final Body body, final AbstractInterpreter<?> interpreter) {
        return interpreter.getContentIfFile(body.getFrom().getFile());
    }

    private String getFromParam(final Body body) {
        Map<String, String> bodyParamMap = body.getParam().stream()
                .collect(Collectors.toMap(Param::getName, Param::getData, (k, v) -> k, LinkedHashMap::new));
        return jacksonService.writeValueAsString(bodyParamMap);
    }

    @RequiredArgsConstructor
    @Getter
    public static class SendGridMethodMetadata {
        private final SendgridInfo httpInfo;
        private final Method httpMethod;
    }
}
