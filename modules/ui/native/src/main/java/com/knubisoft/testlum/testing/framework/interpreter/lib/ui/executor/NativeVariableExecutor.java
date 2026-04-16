package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarMethod;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarPredicate;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import com.knubisoft.testlum.testing.model.scenario.NativeVar;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ExecutorForClass(NativeVar.class)
public class NativeVariableExecutor extends AbstractVariableExecutor<NativeVar> {

    public NativeVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected Map<VarPredicate<NativeVar>, VarMethod<NativeVar>> buildVarToMethodMap() {
        return Map.of(
                var -> Objects.nonNull(var.getElement()), this::getElementResult,
                var -> Objects.nonNull(var.getPath()), (var, result) -> getPathResult(var, result, var.getPath()),
                var -> Objects.nonNull(var.getConstant()),
                        (var, result) -> getConstantResult(var, result, var.getConstant()),
                var -> Objects.nonNull(var.getExpression()),
                        (var, result) -> getExpressionResult(var, result, var.getExpression()),
                var -> Objects.nonNull(var.getFile()),
                        (var, result) -> getFileResult(var, result, var.getFile()),
                var -> Objects.nonNull(var.getSql()),
                        (var, result) -> getSQLResult(var, result, var.getSql()),
                var -> Objects.nonNull(var.getGenerate()),
                        (var, result) -> getRandomGenerateResult(var, result, var.getGenerate()));
    }

    @Override
    protected String getVarName(final NativeVar var) {
        return var.getName();
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
}
