package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_CONDITION_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CONDITION;

@Slf4j
@InterpreterForClass(Condition.class)
public class ConditionInterpreter extends AbstractInterpreter<Condition> {

    public ConditionInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Condition condition, final CommandResult result) {
        try {
            setConditionResult(condition, result);
        } catch (Exception e) {
            log.info(FAILED_CONDITION_WITH_PATH_LOG, condition.getName(), condition.getComment());
            throw e;
        }
    }

    private void setConditionResult(final Condition condition, final CommandResult result) {
        Boolean conditionResult = getConditionFromSpel(condition, result);
        dependencies.getScenarioContext().setCondition(condition.getName(), conditionResult);
        LogUtil.logConditionInfo(condition.getName(), conditionResult);
    }

    private Boolean getConditionFromSpel(final Condition condition, final CommandResult result) {
        String injectedExpression = inject(condition.getSpel());
        Expression exp = new SpelExpressionParser().parseExpression(injectedExpression);
        Boolean conditionResult = Objects.requireNonNull(exp.getValue(Boolean.class));
        ResultUtil.addConditionMetaData(CONDITION, condition.getName(), injectedExpression, conditionResult, result);
        return conditionResult;
    }
}
