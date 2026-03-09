package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FAILED_CONDITION_EXPRESSION;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionUtil {

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'((?:''|[^'])*)'");
    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    private final ResultUtil resultUtil;
    private final LogUtil logUtil;

    public boolean isTrue(final String condition, final ScenarioContext context, final CommandResult result) {
        if (StringUtils.isNotBlank(condition)) {
            String injectedCondition = context.getCondition(condition);
            boolean conditionResult = parseFromSpel(condition, injectedCondition);
            logUtil.logCondition(condition, conditionResult);
            resultUtil.addCommandOnConditionMetaData(condition, conditionResult, result);
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
        logUtil.logConditionInfo(condition, expression, conditionResult);
        resultUtil.addConditionMetaData(condition, expression, conditionResult, result);
    }

    private boolean parseFromSpel(final String condition, final String expression) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            String sanitizedExpression = parameterizeExpression(expression, context);
            return Boolean.TRUE.equals(
                    SPEL_EXPRESSION_PARSER.parseExpression(sanitizedExpression).getValue(context, Boolean.class)
            );
        } catch (Exception e) {
            throw new DefaultFrameworkException(FAILED_CONDITION_EXPRESSION, condition, expression, e.getMessage());
        }
    }

    private String parameterizeExpression(final String expression, final StandardEvaluationContext context) {
        Matcher matcher = STRING_LITERAL_PATTERN.matcher(expression);
        StringBuilder result = new StringBuilder();
        Map<String, String> variables = new HashMap<>();
        AtomicInteger index = new AtomicInteger(1);

        while (matcher.find()) {
            String rawValue = matcher.group(1).replace("''", "'");
            String varName = variables.computeIfAbsent(rawValue, v -> "var" + index.getAndIncrement());
            context.setVariable(varName, rawValue);
            matcher.appendReplacement(result, Matcher.quoteReplacement("#" + varName));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}