package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Component
public class ConditionHelper {

    public boolean getConditionFromSpel(final String injectedExpression,
                                        final String name,
                                        final CommandResult result) {
        Expression exp = new SpelExpressionParser().parseExpression(injectedExpression);
        boolean conditionResult = Boolean.TRUE.equals(exp.getValue(Boolean.class));
        LogUtil.logConditionInfo(name, injectedExpression, conditionResult);
        ResultUtil.addConditionMetaData(name, injectedExpression, conditionResult, result);
        return conditionResult;
    }
}
