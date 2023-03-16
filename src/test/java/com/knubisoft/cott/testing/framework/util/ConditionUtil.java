package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.CONDITION_FALSE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.CONDITION_LOG;

@Slf4j
@UtilityClass
public class ConditionUtil {

    public void validateCondition(final String conditionResult,
                                  final ScenarioContext context) {
        if (conditionResult != null) {
            checkConditionResult(context.getCondition(conditionResult));
        }
    }

    public void checkConditionResult(final Boolean conditionResult) {
        if (conditionResult) {
            log.info(CONDITION_LOG, String.valueOf(true).toUpperCase(Locale.ENGLISH));
        } else {
            log.info(CONDITION_LOG, String.valueOf(false).toUpperCase(Locale.ENGLISH));
            throw new DefaultFrameworkException(CONDITION_FALSE);
        }
    }
}
