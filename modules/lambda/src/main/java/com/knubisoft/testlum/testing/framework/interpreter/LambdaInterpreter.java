package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.LogUtil;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Header;
import com.knubisoft.testlum.testing.model.scenario.Lambda;
import com.knubisoft.testlum.testing.model.scenario.LambdaBody;
import com.knubisoft.testlum.testing.model.scenario.Response;
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
    private Map<AliasEnv, LambdaClient> awsLambdaClients;

    public LambdaInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Lambda o, final CommandResult result) {
        Lambda lambda = injectCommand(o);
        String payload = getPayload(lambda.getBody());
        ResultUtil.addLambdaGeneralMetaData(lambda.getAlias(), lambda.getFunctionName(), payload, result);
        LogUtil.logLambdaInfo(lambda.getAlias(), lambda.getFunctionName(), payload);

        InvokeResponse response = getLambdaFunctionResponse(lambda, payload);
        compareResult(lambda.getResponse(), response, result);
        setContextBody(response.payload().asUtf8String());
    }

    private String getPayload(final LambdaBody body) {
        return StringUtils.isNotBlank(body.getRaw())
                ? body.getRaw()
                : getContentIfFile(body.getFrom().getFile());
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
            AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
            return awsLambdaClients.get(aliasEnv).invoke(request);
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
                : getContentIfFile(expected.getFile());
        result.setActual(StringPrettifier.asJsonResult(actualBody));
        result.setExpected(StringPrettifier.asJsonResult(body));
        httpValidator.validateBody(body, actualBody);
    }

    private void validateHeaders(final Response expected,
                                 final Map<String, List<String>> actualHeaders,
                                 final HttpValidator httpValidator) {
        if (!expected.getHeader().isEmpty()) {
            Map<String, String> actualHeaderMap = actualHeaders.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> String.join(DelimiterConstant.SEMICOLON, entry.getValue())));
            httpValidator.validateHeaders(getExpectedHeaders(expected), actualHeaderMap);
        }
    }

    private Map<String, String> getExpectedHeaders(final Response expected) {
        return expected.getHeader().stream()
                .collect(Collectors.toMap(Header::getName, Header::getData));
    }
}
