package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Slf4j
@UtilityClass
public class ConditionUtil {

    public boolean isTrue(final String conditionName, final ScenarioContext context, final CommandResult result) {
        if (StringUtils.isNotBlank(conditionName)) {
            boolean conditionResult = context.getCondition(conditionName);
            LogUtil.logCondition(conditionName, conditionResult);
            ResultUtil.addCommandOnConditionMetaData(conditionName, conditionResult, result);
            return conditionResult;
        }
        return true;
    }

    public boolean parseFromSpel(final String expression, final String name, final CommandResult result) {
        Expression exp = new SpelExpressionParser().parseExpression(expression);
        boolean conditionResult = Boolean.TRUE.equals(exp.getValue(Boolean.class));
        LogUtil.logConditionInfo(name, expression, conditionResult);
        ResultUtil.addConditionMetaData(name, expression, conditionResult, result);
        return conditionResult;
    }
}
