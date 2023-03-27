package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.VariableService;
import com.knubisoft.cott.testing.model.scenario.Var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;
import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {
    private final Map<VarFromPredicate, VarFromMethod> varToMethodMap;
    @Autowired
    private VariableService variableService;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<VarFromPredicate, VarFromMethod> generalVarMap = new HashMap<>();
        generalVarMap.put(var -> nonNull(var.getSql()), this::getSQLResult);
        generalVarMap.put(var -> nonNull(var.getFile()), this::getFileResult);
        generalVarMap.put(var -> nonNull(var.getExpression()), this::getExpressionResult);
        generalVarMap.put(var -> nonNull(var.getPath()), this::getPathResult);
        generalVarMap.put(var -> nonNull(var.getGenerate()), this::getGenerateResult);
        varToMethodMap = Collections.unmodifiableMap(generalVarMap);
    }

    @Override
    protected void acceptImpl(final Var var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_WITH_PATH_LOG, var.getName(), var.getComment());
            throw e;
        }
    }

    private void setContextVariable(final Var var, final CommandResult result) {
        String value = getValueForContext(var, result);
        dependencies.getScenarioContext().set(var.getName(), value);
        LogUtil.logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final Var var, final CommandResult result) {
        return varToMethodMap.keySet().stream()
                .filter(key -> key.test(var))
                .findFirst()
                .map(varToMethodMap::get)
                .orElseThrow(() -> new DefaultFrameworkException("Type of 'Var' tag is not supported"))
                .apply(var, result);
    }

    private String getPathResult(final Var var, final CommandResult commandResult) {
        return variableService.getPathResult(var.getPath(),
                var.getName(), commandResult, dependencies.getScenarioContext());
    }

    private String getExpressionResult(final Var var, final CommandResult commandResult) {
        return variableService.getExpressionResult(var.getExpression(),
                var.getName(), commandResult, dependencies.getScenarioContext());
    }

    private String getFileResult(final Var var, final CommandResult commandResult) {
        return variableService.getFileResult(var.getFile(),
                dependencies.getFile(), var.getName(), commandResult);
    }

    private String getSQLResult(final Var var, final CommandResult commandResult) {
        return variableService.getSQLResult(var.getSql(), var.getName(),
                commandResult, dependencies.getScenarioContext());
    }

    private String getGenerateResult(final Var var, final CommandResult commandResult) {
        return variableService.getGenerateResult(var.getGenerate(), var.getName(), commandResult);
    }

    private interface VarFromPredicate extends Predicate<Var> { }

    private interface VarFromMethod extends BiFunction<Var, CommandResult, String> { }

}
