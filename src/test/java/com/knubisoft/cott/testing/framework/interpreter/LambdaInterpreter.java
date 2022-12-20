package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.constant.LogMessage;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Lambda;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import java.util.Map;

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
        final String payload = inject(getContentIfFile(lambda.getPayload()));
        ResultUtil.addLambdaGeneralMetaData(lambda.getAlias(), lambda.getFunctionName(), payload, result);
        LogUtil.logLambdaInfo(lambda.getAlias(), lambda.getFunctionName(), payload);

        final String response = getLambdaFunctionResult(lambda, payload);
        compareResult(lambda, response, result);
        setContextBody(response);
    }

    private String getLambdaFunctionResult(final Lambda lambda, final String payload) {
        InvokeRequest request = InvokeRequest.builder()
                .functionName(lambda.getFunctionName())
                .payload(SdkBytes.fromUtf8String(payload))
                .build();
        InvokeResponse response = invokeLambdaFunction(request, lambda.getAlias());
        return response.payload().asUtf8String();
    }

    private InvokeResponse invokeLambdaFunction(final InvokeRequest request, final String alias) {
        try {
            return awsLambdaClients.get(alias).invoke(request);
        } catch (LambdaException e) {
            log.error(LogMessage.ERROR_LOG, e);
            throw e;
        }
    }

    private void compareResult(final Lambda lambda, final String response, final CommandResult result) {
        CompareBuilder compare = newCompare()
                .withActual(response)
                .withExpectedFile(lambda.getFile());

        result.setActual(PrettifyStringJson.getJSONResult(response));
        result.setExpected(PrettifyStringJson.getJSONResult(compare.getExpected()));
        compare.exec();
    }
}
