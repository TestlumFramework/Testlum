package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.e2e.testing.model.scenario.Header;
import com.knubisoft.e2e.testing.model.scenario.Sendgrid;
import com.knubisoft.e2e.testing.model.scenario.SendgridWithBody;
import com.knubisoft.e2e.testing.model.scenario.SendgridWithoutBody;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.HttpValidator;
import com.knubisoft.e2e.testing.framework.util.SendGridUtil;
import com.knubisoft.e2e.testing.model.scenario.Body;
import com.knubisoft.e2e.testing.model.scenario.SendgridInfo;
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

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;

@Slf4j
@InterpreterForClass(Sendgrid.class)
public class SendGridInterpreter extends AbstractInterpreter<Sendgrid> {

    @Autowired(required = false)
    private Map<String, SendGrid> sendGrid;

    public SendGridInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    //CHECKSTYLE:OFF
    @Override
    protected void acceptImpl(final Sendgrid sendgrid, final CommandResult result) {
        log.info(ALIAS_LOG, sendgrid.getAlias());
        SendGridUtil.SendGridMethodMetadata metadata = SendGridUtil.getSendgridMethodMetadata(sendgrid);
        SendgridInfo sendgridInfo = metadata.getHttpInfo();
        Method method = metadata.getHttpMethod();
        String url = inject(sendgridInfo.getUrl());
        result.put("url", url);
        result.put("method", method.name());
        ApiResponse expected = getExpected(sendgridInfo);
        result.setExpected(expected.getBody().toString());
        result.put("expected_code", expected.getCode());
        Response actual = getActual(sendgridInfo, method, sendgrid.getAlias());
        result.setActual(actual.getBody());
        result.put("actual_code", actual.getStatusCode());
        compare(expected, actual);
        setContextBody(actual.getBody());
    }

    //CHECKSTYLE:ON
    protected ApiResponse getExpected(final SendgridInfo sendgridInfo) {
        com.knubisoft.e2e.testing.model.scenario.Response response = sendgridInfo.getResponse();
        Map<String, String> headers = getHeaders(sendgridInfo);
        if (response.getFile() == null) {
            return new ApiResponse(response.getCode(), headers, StringUtils.EMPTY);
        }
        String body = dependencies.getFileSearcher().searchFileToString(response.getFile());
        return new ApiResponse(response.getCode(), headers, body);
    }

    @SneakyThrows
    private Response getActual(final SendgridInfo sendgridInfo, final Method method, final String alias) {
        Request request = new Request();
        request.setMethod(method);
        request.setEndpoint(sendgridInfo.getUrl());
        request.setBody(getBody(sendgridInfo));
        return sendGrid.get(alias).api(request);
    }

    private void compare(final ApiResponse expected, final Response actual) {
        HttpValidator httpValidator = new HttpValidator(this);
        httpValidator.validateCode(expected.getCode(), actual.getStatusCode());
        httpValidator.validateBody(expected.getBody().toString(), actual.getBody());
        httpValidator.validateHeaders(expected.getHeaders(), actual.getHeaders());
        httpValidator.rethrowOnErrors();
    }

    private Map<String, String> getHeaders(final SendgridInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        fillHeadersMap(httpInfo, headers, authorization);
        return SendGridUtil.injectAndGetHeaders(headers, this);
    }

    private void fillHeadersMap(final SendgridInfo info,
                                final Map<String, String> headers,
                                final InterpreterDependencies.Authorization authorization) {
        if (authorization != null && !authorization.getHeaders().isEmpty()) {
            headers.putAll(authorization.getHeaders());
        }
        for (Header header : info.getHeader()) {
            headers.put(header.getName(), header.getData());
        }
    }

    private String getBody(final SendgridInfo sendgridInfo) {
        if (sendgridInfo instanceof SendgridWithoutBody) {
            return null;
        }
        SendgridWithBody commandWithBody = (SendgridWithBody) sendgridInfo;
        Body body = commandWithBody.getBody();
        return SendGridUtil.extractBody(body, this, dependencies);
    }
}
