package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.util.HttpValidator;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.SendGridUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Body;
import com.knubisoft.testlum.testing.model.scenario.Sendgrid;
import com.knubisoft.testlum.testing.model.scenario.SendgridInfo;
import com.knubisoft.testlum.testing.model.scenario.SendgridWithBody;
import com.knubisoft.testlum.testing.model.scenario.SendgridWithoutBody;
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

import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ACTUAL_CODE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONTENT_TO_SEND;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.EXPECTED_CODE;

@Slf4j
@InterpreterForClass(Sendgrid.class)
public class SendGridInterpreter extends AbstractInterpreter<Sendgrid> {

    @Autowired(required = false)
    private Map<AliasEnv, SendGrid> sendGrid;

    public SendGridInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Sendgrid o, final CommandResult result) {
        Sendgrid sendgrid = injectCommand(o);
        SendGridUtil.SendGridMethodMetadata metadata = SendGridUtil.getSendgridMethodMetadata(sendgrid);
        SendgridInfo sendgridInfo = metadata.getHttpInfo();
        Method method = metadata.getHttpMethod();
        Map<String, String> headers = getHeaders(sendgridInfo);
        ResultUtil.addSendGridMetaData(sendgrid.getAlias(), method.name(), headers, sendgridInfo.getEndpoint(), result);
        ApiResponse expected = getExpected(sendgridInfo, headers);
        Response actual = getActual(sendgridInfo, method, sendgrid.getAlias(), result);
        compare(expected, actual, result);
        setContextBody(actual.getBody());
    }

    private ApiResponse getExpected(final SendgridInfo sendgridInfo, final Map<String, String> headers) {
        com.knubisoft.testlum.testing.model.scenario.Response response = sendgridInfo.getResponse();
        String body = StringUtils.isBlank(response.getFile())
                ? DelimiterConstant.EMPTY
                : FileSearcher.searchFileToString(response.getFile(), dependencies.getFile());
        return new ApiResponse(response.getCode(), headers, body);
    }

    @SneakyThrows
    private Response getActual(final SendgridInfo sendgridInfo,
                               final Method method,
                               final String alias,
                               final CommandResult result) {
        String body = getBody(sendgridInfo);
        Request request = new Request();
        request.setMethod(method);
        request.setEndpoint(sendgridInfo.getEndpoint());
        request.setBody(body);
        result.put(CONTENT_TO_SEND, StringPrettifier.asJsonResult(body));
        LogUtil.logHttpInfo(alias, method.name(), sendgridInfo.getEndpoint());
        LogUtil.logBody(request.getBody());
        return sendGrid.get(new AliasEnv(alias, dependencies.getEnvironment())).api(request);
    }

    private void compare(final ApiResponse expected, final Response actual, final CommandResult result) {
        String expectedBody = toString(expected.getBody());
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

    private String getBody(final SendgridInfo sendgridInfo) {
        if (sendgridInfo instanceof SendgridWithoutBody) {
            return null;
        }
        SendgridWithBody commandWithBody = (SendgridWithBody) sendgridInfo;
        Body body = commandWithBody.getBody();
        return SendGridUtil.extractBody(body, dependencies);
    }
}
