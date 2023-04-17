package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.VariableHelper;
import com.knubisoft.cott.testing.framework.util.VariableHelper.VarMethod;
import com.knubisoft.cott.testing.framework.util.VariableHelper.VarPredicate;
import com.knubisoft.cott.testing.model.scenario.Var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_LOG;
import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {

    private final Map<VarPredicate<Var>, VarMethod<Var>> varToMethodMap;
    @Autowired
    private VariableHelper variableHelper;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<VarPredicate<Var>, VarMethod<Var>> varMap = new HashMap<>();
        varMap.put(var -> nonNull(var.getSql()), this::getSQLResult);
        varMap.put(var -> nonNull(var.getFile()), this::getFileResult);
        varMap.put(var -> nonNull(var.getExpression()), this::getExpressionResult);
        varMap.put(var -> nonNull(var.getPath()), this::getPathResult);
        varMap.put(var -> nonNull(var.getGenerate()), this::getRandomGenerateResult);
        varToMethodMap = Collections.unmodifiableMap(varMap);
    }

    @Override
    protected void acceptImpl(final Var var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_LOG, var.getName(), var.getComment());
            throw e;
        }
    }

    private void setContextVariable(final Var var, final CommandResult result) {
        String value = getValueForContext(var, result);
        dependencies.getScenarioContext().set(var.getName(), value);
        LogUtil.logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final Var var, final CommandResult result) {
        return variableHelper.lookupVarMethod(varToMethodMap, var)
                .apply(var, result);
    }

    private String getRandomGenerateResult(final Var var, final CommandResult result) {
        return variableHelper.getGenerateResult(var.getGenerate(), var.getName(), result);
    }

    private String getPathResult(final Var var, final CommandResult result) {
        return variableHelper.getPathResult(var.getPath(), var.getName(), dependencies.getScenarioContext(), result);
    }

    private String getExpressionResult(final Var var, final CommandResult result) {
        return variableHelper.getExpressionResult(
                var.getExpression(), var.getName(), dependencies.getScenarioContext(), result);
    }

    private String getFileResult(final Var var, final CommandResult result) {
        return variableHelper.getFileResult(var.getFile(), dependencies.getFile(), var.getName(), result);
    }

    private String getSQLResult(final Var var, final CommandResult result) {
        return variableHelper.getSQLResult(var.getSql(), var.getName(), dependencies.getScenarioContext(), result);
    }
}
