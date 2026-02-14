package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.*;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(Sendgrid.class)
public class SendGridInterpreter extends AbstractInterpreter<Sendgrid> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String HTTP_METHOD_LOG = format(TABLE_FORMAT, "HTTP method", "{}");
    private static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    private static final String BODY_LOG = format(TABLE_FORMAT, "Body", "{}");
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String CONTENT_FORMAT = format("%n%19s| %-23s|", EMPTY, EMPTY);
    private static final String CONTENT_TO_SEND = "Content to send";
    private static final String EXPECTED_CODE = "Expected code";
    private static final String ACTUAL_CODE = "Actual code";

    //RESULT
    private static final String ALIAS = "Alias";
    private static final String ENDPOINT = "Endpoint";
    private static final String HTTP_METHOD = "HTTP method";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, SendGrid> sendGrid;

    public SendGridInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Sendgrid o, final CommandResult result) {
        Sendgrid sendgrid = injectCommand(o);
        checkAlias(sendgrid);
        SendGridUtil.SendGridMethodMetadata metadata = SendGridUtil.getSendgridMethodMetadata(sendgrid);
        String endpoint = metadata.getHttpInfo().getEndpoint();
        SendgridInfo sendgridInfo = metadata.getHttpInfo();
        Method method = metadata.getHttpMethod();
        Map<String, String> headers = getHeaders(sendgridInfo);
        addSendGridMetaData(sendgrid.getAlias(), method.name(), headers, endpoint, result);
        Response actual = getActual(sendgridInfo, method, sendgrid.getAlias(), endpoint, result);
        ApiResponse expected = getExpected(sendgridInfo, headers);
        compare(expected, actual, result);
        setContextBody(getContextBodyKey(sendgridInfo.getResponse().getFile()), actual.getBody());
    }

    private void checkAlias(final Sendgrid sendgrid) {
        if (sendgrid.getAlias() == null) {
            sendgrid.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private ApiResponse getExpected(final SendgridInfo sendgridInfo, final Map<String, String> headers) {
        com.knubisoft.testlum.testing.model.scenario.Response response = sendgridInfo.getResponse();
        String body = StringUtils.isBlank(response.getFile())
                ? DelimiterConstant.EMPTY
                : getContentIfFile(response.getFile());
        return new ApiResponse(response.getCode(), headers, StringPrettifier.asJsonResult(body));
    }

    @SneakyThrows
    private Response getActual(final SendgridInfo sendgridInfo,
                               final Method method,
                               final String alias,
                               final String endpoint,
                               final CommandResult result) {
        String body = getBody(sendgridInfo, method);
        Request request = getRequest(body, method, sendgridInfo, endpoint);
        result.put(CONTENT_TO_SEND, StringPrettifier.asJsonResult(body));
        logHttpInfo(alias, method.name(), endpoint);
        logBody(request.getBody());
        return sendGrid.get(new AliasEnv(alias, dependencies.getEnvironment())).api(request);
    }

    private void compare(final ApiResponse expected, final Response actual, final CommandResult result) {
        String expectedBody = expected.getBody();
        result.setExpected(StringPrettifier.asJsonResult(expectedBody));
        result.setActual(StringPrettifier.asJsonResult(actual.getBody()));
        result.put(EXPECTED_CODE, expected.getCode());
        result.put(ACTUAL_CODE, actual.getStatusCode());

        HttpValidator httpValidator = new HttpValidator(this);
        httpValidator.validateCode(expected.getCode(), actual.getStatusCode());
        httpValidator.validateBody(expectedBody, actual.getBody());
        httpValidator.validateHeaders(expected.getHeaders(), actual.getHeaders());
        httpValidator.rethrowOnErrors();
    }

    private Map<String, String> getHeaders(final SendgridInfo sendgridInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        HttpUtil.fillHeadersMap(sendgridInfo.getHeader(), headers, dependencies.getAuthorization());
        return headers;
    }

    private String getBody(final SendgridInfo sendgridInfo, final Method method) {
        if (method.equals(Method.GET) || method.equals(Method.DELETE)) {
            return null;
        }
        SendgridWithBody commandWithBody = (SendgridWithBody) sendgridInfo;
        Body body = commandWithBody.getBody();
        return SendGridUtil.extractBody(body, this);
    }

    private Request getRequest(final String body,
                               final Method method,
                               final SendgridInfo sendgridInfo,
                               final String endpoint) {
        Request request = new Request();
        request.setMethod(method);
        request.setEndpoint(endpoint);
        if (nonNull(body)) {
            request.setBody(body);
        }
        if (nonNull(sendgridInfo.getQueryParam())) {
            sendgridInfo.getQueryParam().forEach(queryParam ->
                    request.addQueryParam(queryParam.getKey(), queryParam.getValue()));
        }
        return request;
    }

    //LOGS
    private void logHttpInfo(final String alias, final String method, final String endpoint) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);
    }

    private void logBody(final String body) {
        if (isNotBlank(body)) {
            log.info(BODY_LOG,
                    StringPrettifier.asJsonResult(StringPrettifier.cut(body))
                            .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    //RESULT
    private void addSendGridMetaData(final String alias,
                                     final String httpMethodName,
                                     final Map<String, String> headers,
                                     final String endpoint,
                                     final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    private void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }
}
