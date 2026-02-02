package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.scenario.Var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Var.class)
public class VariableInterpreter extends AbstractInterpreter<Var> {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String NAME_LOG = format(TABLE_FORMAT, "Name", "{}");
    private static final String VALUE_LOG = format(TABLE_FORMAT, "Value", "{}");

    private static final String FAILED_VARIABLE_LOG = "Failed variable <{}> comment <{}>";

    private final Map<VariableHelper.VarPredicate<Var>, VariableHelper.VarMethod<Var>> varToMethodMap;
    @Autowired
    private VariableHelper variableHelper;

    public VariableInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        varToMethodMap = Map.of(variable -> nonNull(variable.getSql()), this::getSQLResult,
                variable -> nonNull(variable.getFile()), this::getFileResult,
                variable -> nonNull(variable.getConstant()), this::getConstantResult,
                variable -> nonNull(variable.getExpression()), this::getExpressionResult,
                variable -> nonNull(variable.getPath()), this::getPathResult,
                variable -> nonNull(variable.getGenerate()), this::getRandomGenerateResult);
    }

    @Override
    protected void acceptImpl(final Var o, final CommandResult result) {
        Var var = injectCommand(o);
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
        logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final Var var, final CommandResult result) {
        return variableHelper.lookupVarMethod(varToMethodMap, var)
                .apply(var, result);
    }

    private String getSQLResult(final Var var, final CommandResult result) {
        return variableHelper.getSQLResult(var.getSql(), var.getName(), result);
    }

    private String getFileResult(final Var var, final CommandResult result) {
        UnaryOperator<String> fileToString = fileName -> {
            String content = FileSearcher.searchFileToString(fileName, dependencies.getFile());
            return inject(content);
        };
        return variableHelper.getFileResult(var.getFile(), var.getName(), fileToString, result);
    }

    private String getConstantResult(final Var var, final CommandResult result) {
        return variableHelper.getConstantResult(var.getConstant(), var.getName(), result);
    }

    private String getExpressionResult(final Var var, final CommandResult result) {
        return variableHelper.getExpressionResult(var.getExpression(), var.getName(), result);
    }

    private String getPathResult(final Var var, final CommandResult result) {
        UnaryOperator<String> fileToString = fileName -> {
            String content = FileSearcher.searchFileToString(fileName, dependencies.getFile());
            return inject(content);
        };
        return variableHelper.getPathResult(var.getPath(), var.getName(), dependencies.getScenarioContext(), result,
                fileToString);
    }

    private String getRandomGenerateResult(final Var var, final CommandResult result) {
        return variableHelper.getRandomGenerateResult(var.getGenerate(), var.getName(), result);
    }

    private void logVarInfo(final String name, final String value) {
        log.info(NAME_LOG, name);
        log.info(VALUE_LOG, StringPrettifier.cut(value));
    }
}
