package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.scenario.Body;
import com.knubisoft.testlum.testing.model.scenario.Param;
import com.knubisoft.testlum.testing.model.scenario.Sendgrid;
import com.knubisoft.testlum.testing.model.scenario.SendgridInfo;
import com.sendgrid.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public String extractBody(final Body body, final InterpreterDependencies dependencies) {
        try {
            return getAppropriateBody(body, dependencies);
        } catch (Exception e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private String getAppropriateBody(final Body body, final InterpreterDependencies dependencies) {
        if (isNull(body)) {
            return StringUtils.EMPTY;
        } else if (nonNull(body.getRaw())) {
            return getFromRaw(body);
        } else if (nonNull(body.getFrom())) {
            return getFromFile(body, dependencies);
        }
        return getFromParam(body);
    }

    private String getFromRaw(final Body body) {
        return body.getRaw();
    }

    private String getFromFile(final Body body, final InterpreterDependencies dependencies) {
        return FileSearcher.searchFileToString(body.getFrom().getFile(), dependencies.getFile());
    }

    private String getFromParam(final Body body) {
        Map<String, String> bodyParamMap = body.getParam().stream()
                .collect(toMap(Param::getName, Param::getData, (k, v) -> k, LinkedHashMap::new));
        return JacksonMapperUtil.writeValueAsString(bodyParamMap);
    }

    @RequiredArgsConstructor
    @Getter
    public static class SendGridMethodMetadata {
        private final SendgridInfo httpInfo;
        private final Method httpMethod;
    }
}
