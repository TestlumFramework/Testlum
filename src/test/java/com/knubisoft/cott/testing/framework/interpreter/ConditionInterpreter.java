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

import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_CONDITION_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.EXPRESSION;

@Slf4j
@InterpreterForClass(Condition.class)
public class ConditionInterpreter extends AbstractInterpreter<Condition> {

    public ConditionInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Condition o, final CommandResult result) {
        try {
            setConditionResult(o, result);
        } catch (Exception e) {
            log.info(FAILED_CONDITION_WITH_PATH_LOG, o.getName(), o.getComment());
            throw e;
        }
    }

    private void setConditionResult(final Condition o, final CommandResult result) {
        Boolean value = getConditionFromSpel(o, result);
        dependencies.getScenarioContext().setCondition(o.getName(), value);
        LogUtil.logVarInfo(o.getName(), String.valueOf(value));
    }

    private Boolean getConditionFromSpel(final Condition condition, final CommandResult result) {
        String expression = condition.getSpel();
        String injectedExpression = inject(expression);
        Expression exp = new SpelExpressionParser().parseExpression(injectedExpression);
        Boolean valueResult = exp.getValue(Boolean.class);
        ResultUtil.addConditionMetaData(EXPRESSION, condition.getName(), expression, valueResult, result);
        return valueResult;
    }
}
