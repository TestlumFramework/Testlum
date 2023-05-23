package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.HttpValidator;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.SendGridUtil;
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
    protected void acceptImpl(final Sendgrid sendgrid, final CommandResult result) {
        String alias = sendgrid.getAlias();
        SendGridUtil.SendGridMethodMetadata metadata = SendGridUtil.getSendgridMethodMetadata(sendgrid);
        SendgridInfo sendgridInfo = metadata.getHttpInfo();
        Method method = metadata.getHttpMethod();
        String endpoint = inject(sendgridInfo.getEndpoint());
        Map<String, String> headers = getHeaders(sendgridInfo);
        ResultUtil.addSendGridMetaData(alias, method.name(), headers, endpoint, result);
        ApiResponse expected = getExpected(sendgridInfo);
        Response actual = getActual(sendgridInfo, method, alias, endpoint, result);
        result.setExpected(StringPrettifier.asJsonResult(toString(expected.getBody())));
        result.setActual(StringPrettifier.asJsonResult(toString(actual.getBody())));
        compare(expected, actual, result);
        setContextBody(actual.getBody());
    }

    private ApiResponse getExpected(final SendgridInfo sendgridInfo) {
        com.knubisoft.testlum.testing.model.scenario.Response response = sendgridInfo.getResponse();
        Map<String, String> headers = getHeaders(sendgridInfo);
        String body = StringUtils.isBlank(response.getFile())
                ? DelimiterConstant.EMPTY : FileSearcher.searchFileToString(response.getFile(), dependencies.getFile());
        return new ApiResponse(response.getCode(), headers, body);
    }

    @SneakyThrows
    private Response getActual(final SendgridInfo sendgridInfo,
                               final Method method,
                               final String alias,
                               final String endpoint,
                               final CommandResult result) {
        String body = getBody(sendgridInfo);
        Request request = new Request();
        request.setMethod(method);
        request.setEndpoint(endpoint);
        request.setBody(body);
        result.put(CONTENT_TO_SEND, StringPrettifier.asJsonResult(body));
        LogUtil.logHttpInfo(alias, method.name(), endpoint);
        LogUtil.logBody(request.getBody());
        return sendGrid.get(new AliasEnv(alias, dependencies.getEnvironment())).api(request);
    }

    private void compare(final ApiResponse expected, final Response actual, final CommandResult result) {
        int expectedCode = expected.getCode();
        int actualCode = actual.getStatusCode();
        result.put(EXPECTED_CODE, expectedCode);
        result.put(ACTUAL_CODE, actualCode);
        HttpValidator httpValidator = new HttpValidator(this);
        httpValidator.validateCode(expected.getCode(), actual.getStatusCode());
        httpValidator.validateBody(expected.getBody().toString(), actual.getBody());
        httpValidator.validateHeaders(expected.getHeaders(), actual.getHeaders());
        httpValidator.rethrowOnErrors();
    }

    private Map<String, String> getHeaders(final SendgridInfo sendgridInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        SendGridUtil.fillHeadersMap(sendgridInfo.getHeader(), headers, authorization);
        return SendGridUtil.injectAndGetHeaders(headers, this);
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
