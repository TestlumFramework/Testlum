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

    public void checkIf(final String ifCondition,
                        final ScenarioContext context) {
        if (ifCondition != null) {
            checkCondition(context.inject(ifCondition));
        }
    }
    public void checkCondition(final String original) {
        if (original.equalsIgnoreCase(String.valueOf(Boolean.TRUE))) {
            log.info(CONDITION_LOG, original.toUpperCase(Locale.ENGLISH));
            return;
        }
        if (original.equalsIgnoreCase(String.valueOf(Boolean.FALSE))) {
            log.info(CONDITION_LOG, original.toUpperCase(Locale.ENGLISH));
            throw new DefaultFrameworkException(CONDITION_FALSE);
        }
    }
}
