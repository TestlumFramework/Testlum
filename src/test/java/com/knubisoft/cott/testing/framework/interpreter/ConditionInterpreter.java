package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.CONDITION_RESULT_WRONG_TYPE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_CONDITION_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.EXPRESSION;
import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Condition.class)
public class ConditionInterpreter extends AbstractInterpreter<Condition> {
    private final Map<ConditionFromPredicate, ConditionFromMethod> conditionsMap;

    public ConditionInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<ConditionFromPredicate, ConditionFromMethod> conditionMap = new HashMap<>();
        conditionMap.put(condition -> nonNull(condition.getName()), this::getConditionFromSpel);
        conditionsMap = Collections.unmodifiableMap(conditionMap);
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
        String value = String.valueOf(getValueForContext(o, result));
        dependencies.getScenarioContext().set(o.getName(), value);
        LogUtil.logVarInfo(o.getName(), value);
    }

    private String getValueForContext(final Condition condition, final CommandResult result) {
        return conditionsMap.keySet().stream()
                .filter(key -> key.test(condition))
                .findFirst()
                .map(conditionsMap::get)
                .orElseThrow(() -> new DefaultFrameworkException("Type of 'Condition' tag is not supported"))
                .apply(condition, result);
    }

    private String getConditionFromSpel(final Condition condition, final CommandResult result) {
        String expression = condition.getSpel();
        String injectedExpression = inject(expression);
        Expression exp = new SpelExpressionParser().parseExpression(injectedExpression);
        String valueResult = Objects.requireNonNull(exp.getValue()).toString();
        if (valueResult.matches("(?i)true|false")) {
            ResultUtil.addVariableMetaData(EXPRESSION, condition.getName(), expression, valueResult, result);
            return valueResult.toUpperCase(Locale.ENGLISH);
        } else {
            throw new DefaultFrameworkException(CONDITION_RESULT_WRONG_TYPE);
        }
    }

    private interface ConditionFromPredicate extends Predicate<Condition> {
    }

    private interface ConditionFromMethod extends BiFunction<Condition, CommandResult, String> {
    }
}
