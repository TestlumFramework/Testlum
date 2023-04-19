package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONDITION_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONDITION;

@Slf4j
@UtilityClass
public class ConditionUtil {

    public boolean isTrue(final String conditionName,
                          final ScenarioContext context,
                          final CommandResult result) {
        if (Objects.nonNull(conditionName)) {
            boolean conditionResult = context.getCondition(conditionName);
            log.info(CONDITION_LOG, conditionName, conditionResult);
            result.put(CONDITION, conditionName + " : " + conditionResult);
            return conditionResult;
        }
        return true;
    }
}
