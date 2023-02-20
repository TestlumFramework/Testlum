package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.HttpUtil;
import com.knubisoft.cott.testing.framework.util.HttpValidator;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Header;
import com.knubisoft.cott.testing.model.scenario.Lambda;
import com.knubisoft.cott.testing.model.scenario.LambdaBody;
import com.knubisoft.cott.testing.model.scenario.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@InterpreterForClass(Lambda.class)
public class LambdaInterpreter extends AbstractInterpreter<Lambda> {

    @Autowired(required = false)
    private Map<String, LambdaClient> awsLambdaClients;

    public LambdaInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Lambda lambda, final CommandResult result) {
        String payload = getPayload(lambda.getBody());
        ResultUtil.addLambdaGeneralMetaData(lambda.getAlias(), lambda.getFunctionName(), payload, result);
        LogUtil.logLambdaInfo(lambda.getAlias(), lambda.getFunctionName(), payload);

        InvokeResponse response = getLambdaFunctionResponse(lambda, payload);
        compareResult(lambda.getResponse(), response, result);
        setContextBody(response.payload().asUtf8String());
    }

    private String getPayload(final LambdaBody body) {
        String payload = StringUtils.isNotBlank(body.getRaw())
                ? body.getRaw()
                : FileSearcher.searchFileToString(body.getFrom().getFile(), dependencies.getFile());
        return inject(payload);
    }

    private InvokeResponse getLambdaFunctionResponse(final Lambda lambda, final String payload) {
        InvokeRequest request = InvokeRequest.builder()
                .functionName(lambda.getFunctionName())
                .payload(SdkBytes.fromUtf8String(payload))
                .build();
        return invokeLambdaFunction(request, lambda.getAlias());
    }

    private InvokeResponse invokeLambdaFunction(final InvokeRequest request, final String alias) {
        try {
            return awsLambdaClients.get(alias).invoke(request);
        } catch (LambdaException e) {
            LogUtil.logError(e);
            throw e;
        }
    }

    private void compareResult(final Response expected, final InvokeResponse response, final CommandResult result) {
        HttpValidator httpValidator = new HttpValidator(this);
        validateBody(expected, response.payload().asUtf8String(), httpValidator, result);
        validateHeaders(expected, response.sdkHttpResponse().headers(), httpValidator);
        httpValidator.validateCode(expected.getCode(), response.sdkHttpResponse().statusCode());
        httpValidator.rethrowOnErrors();
    }

    private void validateBody(final Response expected,
                              final String actualBody,
                              final HttpValidator httpValidator,
                              final CommandResult result) {
        String body = StringUtils.isBlank(expected.getFile())
                ? DelimiterConstant.EMPTY
                : FileSearcher.searchFileToString(expected.getFile(), dependencies.getFile());
        result.setActual(PrettifyStringJson.getJSONResult(actualBody));
        result.setExpected(PrettifyStringJson.getJSONResult(body));
        httpValidator.validateBody(body, actualBody);
    }

    private void validateHeaders(final Response expected,
                                 final Map<String, List<String>> actualHeaders,
                                 final HttpValidator httpValidator) {
        if (!expected.getHeader().isEmpty()) {
            Map<String, String> actualHeaderMap = actualHeaders.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> String.join(DelimiterConstant.SEMICOLON, entry.getValue())));
            Map<String, String> expectedHeaderMap = getInjectedHeaders(expected);
            httpValidator.validateHeaders(expectedHeaderMap, actualHeaderMap);
        }
    }

    private Map<String, String> getInjectedHeaders(final Response expected) {
        Map<String, String> headers = expected.getHeader().stream()
                .collect(Collectors.toMap(Header::getName, Header::getData));
        headers.replaceAll((name, data) -> getContentIfFile(data));
        return HttpUtil.injectAndGetHeaders(headers, this);
    }
}
