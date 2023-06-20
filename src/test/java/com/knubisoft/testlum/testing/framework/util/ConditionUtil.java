package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_CONDITION_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONDITION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NAME_VALUE;
import static java.lang.String.format;

@Slf4j
@UtilityClass
public class ConditionUtil {

    public boolean isTrue(final String condition, final ScenarioContext context, final CommandResult result) {
        if (StringUtils.isNotBlank(condition)) {
            String injectedCondition = context.getCondition(condition);
            boolean conditionResult = parseFromSpel(condition, injectedCondition);
            LogUtil.logCondition(condition, conditionResult);
            result.put(CONDITION, format(NAME_VALUE, condition, conditionResult));
            return conditionResult;
        }
        return true;
    }

    public void processCondition(final String condition,
                                 final String expression,
                                 final CommandResult result,
                                 final ScenarioContext scenarioContext) {
        boolean conditionResult = parseFromSpel(condition, expression);
        scenarioContext.setCondition(condition, conditionResult);
        LogUtil.logConditionInfo(condition, expression, conditionResult);
        ResultUtil.addConditionMetaData(condition, expression, conditionResult, result);
    }

    private boolean parseFromSpel(final String condition, final String expression) {
        try {
            Expression exp = new SpelExpressionParser().parseExpression(expression);
            return Boolean.TRUE.equals(exp.getValue(Boolean.class));
        } catch (Exception e) {
            log.info(FAILED_CONDITION_LOG, condition, expression);
            throw e;
        }
    }
}
