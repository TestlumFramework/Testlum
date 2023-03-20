package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.CONDITION_LOG;

@Slf4j
@UtilityClass
public class ConditionUtil {

    public boolean validateCondition(final String conditionResult,
                                     final ScenarioContext context) {
        if (!Objects.isNull(conditionResult) && !context.getCondition(conditionResult)) {
            log.info(CONDITION_LOG, conditionResult, Boolean.FALSE);
            return false;
        }
        return true;
    }
}
