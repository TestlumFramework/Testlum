package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarMethod;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarPredicate;
import com.knubisoft.testlum.testing.model.scenario.NativeVar;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_VARIABLE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ELEMENT_PRESENT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LOCATOR_FORM;
import static java.util.Objects.nonNull;


@Slf4j
@ExecutorForClass(NativeVar.class)
public class NativeVariableExecutor extends AbstractUiExecutor<NativeVar> {

    private final Map<VarPredicate<NativeVar>, VarMethod<NativeVar>> varToMethodMap;
    @Autowired
    private VariableHelper variableHelper;

    public NativeVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        varToMethodMap = Map.of(
                var -> nonNull(var.getElement()), this::getElementResult,
                var -> nonNull(var.getPath()), this::getPathResult,
                var -> nonNull(var.getConstant()), this::getConstantResult,
                var -> nonNull(var.getExpression()), this::getExpressionResult,
                var -> nonNull(var.getFile()), this::getFileResult,
                var -> nonNull(var.getSql()), this::getSQLResult,
                var -> nonNull(var.getGenerate()), this::getRandomGenerateResult);
    }

    @Override
    public void execute(final NativeVar var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_LOG, var.getName(), var.getComment());
            throw e;
        }
    }

    private void setContextVariable(final NativeVar var, final CommandResult result) {
        String value = getValueForContext(var, result);
        dependencies.getScenarioContext().set(var.getName(), value);
        LogUtil.logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final NativeVar var, final CommandResult result) {
        return variableHelper.lookupVarMethod(varToMethodMap, var).apply(var, result);
    }

    private String getElementResult(final NativeVar var, final CommandResult result) {
        String valueResult;
        String locatorId = var.getElement().getPresent().getLocatorId();
        try {
            UiUtil.findWebElement(dependencies, locatorId);
            valueResult = String.valueOf(true);
        } catch (NoSuchElementException e) {
            valueResult = String.valueOf(false);
        }
        ResultUtil.addVariableMetaData(ELEMENT_PRESENT, var.getName(), LOCATOR_FORM, locatorId, valueResult, result);
        return valueResult;
    }

    private String getPathResult(final NativeVar var, final CommandResult result) {
        return variableHelper.getPathResult(var.getPath(), var.getName(), dependencies.getScenarioContext(), result);
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
