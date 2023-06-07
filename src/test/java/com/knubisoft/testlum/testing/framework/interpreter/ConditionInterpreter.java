package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_CONDITION_LOG;

@Slf4j
@InterpreterForClass(Condition.class)
public class ConditionInterpreter extends AbstractInterpreter<Condition> {

    public ConditionInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Condition o, final CommandResult result) {
        Condition condition = injectCommand(o);
        try {
            setConditionResult(condition, result);
        } catch (Exception e) {
            log.info(FAILED_CONDITION_LOG, condition.getName(), condition.getSpel());
            throw e;
        }
    }

    private void setConditionResult(final Condition condition, final CommandResult result) {
        boolean conditionResult = getConditionFromSpel(condition, result);
        dependencies.getScenarioContext().setCondition(condition.getName(), conditionResult);
    }

    private boolean getConditionFromSpel(final Condition condition, final CommandResult result) {
        String injectedExpression = inject(condition.getSpel());
        Expression exp = new SpelExpressionParser().parseExpression(injectedExpression);
        boolean conditionResult = Boolean.TRUE.equals(exp.getValue(Boolean.class));
        LogUtil.logConditionInfo(condition.getName(), injectedExpression, conditionResult);
        ResultUtil.addConditionMetaData(condition.getName(), injectedExpression, conditionResult, result);
        return conditionResult;
    }
}
