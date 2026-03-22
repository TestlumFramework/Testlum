package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarMethod;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarPredicate;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import com.knubisoft.testlum.testing.model.scenario.NativeVar;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

@Slf4j
@ExecutorForClass(NativeVar.class)
public class NativeVariableExecutor extends AbstractUiExecutor<NativeVar> {

    private final Map<VarPredicate<NativeVar>, VarMethod<NativeVar>> varToMethodMap;
    private final VariableHelper variableHelper;

    public NativeVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.varToMethodMap = Map.of(
                var -> Objects.nonNull(var.getElement()), this::getElementResult,
                var -> Objects.nonNull(var.getPath()), this::getPathResult,
                var -> Objects.nonNull(var.getConstant()), this::getConstantResult,
                var -> Objects.nonNull(var.getExpression()), this::getExpressionResult,
                var -> Objects.nonNull(var.getFile()), this::getFileResult,
                var -> Objects.nonNull(var.getSql()), this::getSQLResult,
                var -> Objects.nonNull(var.getGenerate()), this::getRandomGenerateResult);
        this.variableHelper = dependencies.getContext().getBean(VariableHelper.class);
    }

    @Override
    public void execute(final NativeVar var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(LogMessage.FAILED_VARIABLE_LOG, var.getName(), var.getComment());
            throw e;
        }
    }

    private void setContextVariable(final NativeVar var, final CommandResult result) {
        String value = getValueForContext(var, result);
        dependencies.getScenarioContext().set(var.getName(), value);
        logUtil.logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final NativeVar var, final CommandResult result) {
        return variableHelper.lookupVarMethod(varToMethodMap, var).apply(var, result);
    }

    private String getElementResult(final NativeVar var, final CommandResult result) {
        String valueResult;
        String locatorId = var.getElement().getPresent().getLocator();
        LocatorStrategy locatorStrategy = var.getElement().getPresent().getLocatorStrategy();
        try {
            uiUtil.findWebElement(dependencies, locatorId, locatorStrategy);
            valueResult = String.valueOf(true);
        } catch (DefaultFrameworkException e) {
            valueResult = String.valueOf(false);
        }
        resultUtil.addVariableMetaData(ResultUtil.ELEMENT_PRESENT, var.getName(),
                ResultUtil.LOCATOR_FORM, locatorId, valueResult, result);
        return valueResult;
    }

    private String getPathResult(final NativeVar var, final CommandResult result) {
        UnaryOperator<String> fileToString = fileName -> {
            String content = fileSearcher.searchFileToString(fileName, dependencies.getFile());
            return inject(content);
        };
        return variableHelper.getPathResult(var.getPath(), var.getName(), dependencies.getScenarioContext(), result,
                fileToString);
    }

    private String getConstantResult(final NativeVar var, final CommandResult result) {
        return variableHelper.getConstantResult(var.getConstant(), var.getName(), result);
    }

    private String getExpressionResult(final NativeVar var, final CommandResult result) {
        return variableHelper.getExpressionResult(var.getExpression(), var.getName(), result);
    }

    private String getFileResult(final NativeVar var, final CommandResult result) {
        return variableHelper.getFileResult(var.getFile(), var.getName(), this::getContentIfFile, result);
    }

    private String getSQLResult(final NativeVar var, final CommandResult result) {
        return variableHelper.getSQLResult(var.getSql(), var.getName(), result);
    }

    private String getRandomGenerateResult(final NativeVar var, final CommandResult result) {
        return variableHelper.getRandomGenerateResult(var.getGenerate(), var.getName(), result);
    }
}
