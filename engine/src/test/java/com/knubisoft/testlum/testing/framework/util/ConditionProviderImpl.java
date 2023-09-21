package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FAILED_CONDITION_EXPRESSION;

@Component
public class ConditionProviderImpl implements ConditionProvider {

    @Override
    public boolean isTrue(final String condition, final ScenarioContext context, final CommandResult result) {
        return ConditionUtil.isTrue(condition, context, result);
    }

    @Override
    public void processCondition(final String condition,
                                 final String expression,
                                 final ScenarioContext scenarioContext,
                                 final CommandResult result) {
        ConditionUtil.processCondition(condition, expression, scenarioContext, result);
    }

    @Slf4j
    @UtilityClass
    public static class ConditionUtil {

        public boolean isTrue(final String condition, final ScenarioContext context, final CommandResult result) {
            if (StringUtils.isNotBlank(condition)) {
                String injectedCondition = context.getCondition(condition);
                boolean conditionResult = parseFromSpel(condition, injectedCondition);
                LogUtil.logCondition(condition, conditionResult);
                ResultUtil.addCommandOnConditionMetaData(condition, conditionResult, result);
                return conditionResult;
            }
            return true;
        }

        public void processCondition(final String condition,
                                     final String expression,
                                     final ScenarioContext scenarioContext,
                                     final CommandResult result) {
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
                throw new DefaultFrameworkException(FAILED_CONDITION_EXPRESSION, condition, expression, e.getMessage());
            }
        }
    }
}
