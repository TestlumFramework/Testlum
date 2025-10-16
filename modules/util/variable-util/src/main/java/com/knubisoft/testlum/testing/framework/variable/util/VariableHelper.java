package com.knubisoft.testlum.testing.framework.variable.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.function.*;

public interface VariableHelper {

    <T extends AbstractCommand> VarMethod<T> lookupVarMethod(Map<VarPredicate<T>, VarMethod<T>> methodMap, T var);

    String getRandomGenerateResult(FromRandomGenerate randomGenerate, String varName, CommandResult result);

    String getFileResult(FromFile fromFile, String varName, UnaryOperator<String> fileToString, CommandResult result);

    String getConstantResult(FromConstant fromConstant, String varName, CommandResult result);

    String getExpressionResult(FromExpression fromExpression, String varName, CommandResult result);

    String getPathResult(FromPath fromPath, String varName, ScenarioContext scenarioContext, CommandResult result);

    String getSQLResult(FromSQL fromSQL, String varName, CommandResult result);

	Supplier<String> getGoogleAuthToken(GoogleAuthToken googleAuthToken, ApplicationContext context, ScenarioContext scenarioContext, String env, String varName, CommandResult result);



    interface VarPredicate<T extends AbstractCommand> extends Predicate<T> { }
    interface VarMethod<T extends AbstractCommand> extends BiFunction<T, CommandResult, String> { }
    interface RandomPredicate extends Predicate<FromRandomGenerate> { }
    interface RandomFunction extends Function<FromRandomGenerate, String> { }
}
