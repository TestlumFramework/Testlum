package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.CONDITION_LOG;

@Slf4j
@UtilityClass
public class ConditionUtil {

    public boolean isTrue(final String conditionName,
                          final ScenarioContext context) {
        if (Objects.nonNull(conditionName) && !context.getCondition(conditionName)) {
            log.info(CONDITION_LOG, conditionName);
            return false;
        }
        return true;
    }
}
