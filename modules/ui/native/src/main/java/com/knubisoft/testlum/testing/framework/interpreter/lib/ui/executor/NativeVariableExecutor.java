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

import static java.util.Map.entry;

@Slf4j
@ExecutorForClass(NativeVar.class)
public class NativeVariableExecutor extends AbstractVariableExecutor<NativeVar> {

    public NativeVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected Map<VarPredicate<NativeVar>, VarMethod<NativeVar>> buildVarToMethodMap() {
        return Map.ofEntries(
                entry(v -> Objects.nonNull(v.getElement()), this::getElementResult),
                entry(v -> Objects.nonNull(v.getPath()), (v, r) -> getPathResult(v, r, v.getPath())),
                entry(v -> Objects.nonNull(v.getConstant()), (v, r) -> getConstantResult(v, r, v.getConstant())),
                entry(v -> Objects.nonNull(v.getExpression()), (v, r) -> getExpressionResult(v, r, v.getExpression())),
                entry(v -> Objects.nonNull(v.getFile()), (v, r) -> getFileResult(v, r, v.getFile())),
                entry(v -> Objects.nonNull(v.getSql()), (v, r) -> getSQLResult(v, r, v.getSql())),
                entry(v -> Objects.nonNull(v.getGenerate()), (v, r) -> getRandomGenerateResult(v, r, v.getGenerate())),
                entry(v -> Objects.nonNull(v.getDate()), (v, r) -> getDateResult(v, r, v.getDate()))
        );
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
