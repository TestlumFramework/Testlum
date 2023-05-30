package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.UiCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_CONDITION_LOG;

@Slf4j
@ExecutorForClass(UiCondition.class)
public class UiConditionExecutor extends AbstractUiExecutor<UiCondition> {

    public UiConditionExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final UiCondition condition, final CommandResult result) {
        try {
            setConditionResult(condition, result);
        } catch (Exception e) {
            log.info(FAILED_CONDITION_LOG, condition.getName(), condition.getSpel());
            throw e;
        }
    }

    private void setConditionResult(final UiCondition condition, final CommandResult result) {
        boolean conditionResult = getConditionFromSpel(condition, result);
        dependencies.getScenarioContext().setCondition(condition.getName(), conditionResult);
    }

    private boolean getConditionFromSpel(final UiCondition condition, final CommandResult result) {
        String injectedExpression = inject(condition.getSpel());
        Expression exp = new SpelExpressionParser().parseExpression(injectedExpression);
        boolean conditionResult = Boolean.TRUE.equals(exp.getValue(Boolean.class));
        LogUtil.logConditionInfo(condition.getName(), injectedExpression, conditionResult);
        ResultUtil.addConditionMetaData(condition.getName(), injectedExpression, conditionResult, result);
        return conditionResult;
    }
}
