package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarMethod;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarPredicate;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.FromConstant;
import com.knubisoft.testlum.testing.model.scenario.FromExpression;
import com.knubisoft.testlum.testing.model.scenario.FromFile;
import com.knubisoft.testlum.testing.model.scenario.FromPath;
import com.knubisoft.testlum.testing.model.scenario.FromRandomGenerate;
import com.knubisoft.testlum.testing.model.scenario.FromSQL;
import com.knubisoft.testlum.testing.model.scenario.FromDate;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.UnaryOperator;

@Slf4j
public abstract class AbstractVariableExecutor<T extends AbstractUiCommand> extends AbstractUiExecutor<T> {

    protected final VariableHelper variableHelper;
    private Map<VarPredicate<T>, VarMethod<T>> varToMethodMap;

    protected AbstractVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.variableHelper = dependencies.getContext().getBean(VariableHelper.class);
        this.varToMethodMap = buildVarToMethodMap();
    }

    protected abstract Map<VarPredicate<T>, VarMethod<T>> buildVarToMethodMap();

    @Override
    protected void execute(final T var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(LogMessage.FAILED_VARIABLE_LOG, getVarName(var), var.getComment());
            throw e;
        }
    }

    private void setContextVariable(final T var, final CommandResult result) {
        String value = variableHelper.lookupVarMethod(varToMethodMap, var).apply(var, result);
        dependencies.getScenarioContext().set(getVarName(var), value);
        logUtil.logVarInfo(getVarName(var), value);
    }

    protected abstract String getVarName(T var);

    protected UnaryOperator<String> fileToStringOperator() {
        return fileName -> {
            String content = fileSearcher.searchFileToString(fileName, dependencies.getFile());
            return inject(content);
        };
    }

    protected String getPathResult(final T var, final CommandResult result, final FromPath path) {
        return variableHelper.getPathResult(path, getVarName(var), dependencies.getScenarioContext(), result,
                fileToStringOperator());
    }

    protected String getConstantResult(final T var, final CommandResult result, final FromConstant constant) {
        return variableHelper.getConstantResult(constant, getVarName(var), result);
    }

    protected String getExpressionResult(final T var, final CommandResult result, final FromExpression expression) {
        return variableHelper.getExpressionResult(expression, getVarName(var), result);
    }

    protected String getFileResult(final T var, final CommandResult result, final FromFile file) {
        return variableHelper.getFileResult(file, getVarName(var), this::getContentIfFile, result);
    }

    protected String getSQLResult(final T var, final CommandResult result, final FromSQL sql) {
        return variableHelper.getSQLResult(sql, getVarName(var), result);
    }

    protected String getRandomGenerateResult(final T var, final CommandResult result,
                                             final FromRandomGenerate generate) {
        return variableHelper.getRandomGenerateResult(generate, getVarName(var), result);
    }

    protected String getDateResult(final T var, final CommandResult result, final FromDate date) {
        return variableHelper.getDateResult(date, getVarName(var), result);
    }
}