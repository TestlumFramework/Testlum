package com.testlum.testing.framework.util;

import com.testlum.log.Color;
import com.testlum.log.LogFormat;
import com.testlum.log.table.Align;
import com.testlum.log.table.DynamicTableBuilder;
import com.testlum.log.table.TableBuilder;
import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Ui;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogUtil {

    private final StringPrettifier stringPrettifier;

    public void logNonParsedScenarioInfo(final String path, final String exception) {
        log.error(LogMessage.INVALID_SCENARIO_LOG, path, exception);
    }

    public void logCondition(final String name, final boolean condition) {
        if (!condition) {
            log.info(LogMessage.COMMAND_SKIPPED_ON_CONDITION_LOG);
        }
        log.info(LogMessage.CONDITION_LOG, name, condition);
    }

    public void logConditionInfo(final String name,
                                 final String expression,
                                 final boolean value) {
        log.info(LogMessage.NAME_LOG, name);
        log.info(LogMessage.EXPRESSION_LOG, expression);
        log.info(LogMessage.VALUE_LOG, value);
    }

    public void logExecutionTime(final long time, final AbstractCommand command) {
        if (Ui.class.isAssignableFrom(command.getClass())) {
            log.info(LogMessage.UI_EXECUTION_TIME_LOG, time);
        } else {
            log.info(LogMessage.EXECUTION_TIME_LOG, time);
        }
    }

    public void logException(final Exception ex) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.error(LogFormat.exceptionLog(),
                    ex.getMessage().replaceAll(LogFormat.newLine(), LogFormat.newLogLine()));
        } else {
            log.error(LogFormat.exceptionLog(), ex.toString());
        }
    }

    public void logAllQueries(final List<String> queries, final String alias) {
        log.info(LogMessage.ALIAS_LOG, alias);
        queries.forEach(query -> log.info(LogMessage.QUERY,
                query.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE)));
    }

    public void logAllQueries(final String dbType, final List<String> queries, final String alias) {
        log.info(LogMessage.DB_TYPE_LOG, dbType);
        logAllQueries(queries, alias);
    }

    public void logVarInfo(final String name, final String value) {
        log.info(LogMessage.NAME_LOG, name);
        log.info(LogMessage.VALUE_LOG, stringPrettifier.cut(value));
    }

    public void logScenarioWithoutTags(final String scenarioPath) {
        log.warn(LogMessage.SCENARIO_WITH_EMPTY_TAG_LOG, scenarioPath);
    }

    public void logEmptyScenariosForTestRails() {
        log.warn(LogMessage.EMPTY_SCENARIOS_FOR_TESTRAIL);
    }

    public void logInvalidScenariosSummary(final Map<String, String> invalid, final Map<String, String> skipped) {
        if (!invalid.isEmpty()) {
            log.error(constructScenarioStatusTable(LogMessage.INVALID_SCENARIOS_TITLE, Color.RED, invalid));
        }
        if (!skipped.isEmpty()) {
            log.warn(constructScenarioStatusTable(LogMessage.SKIPPED_SCENARIOS_TITLE, Color.YELLOW, skipped));
        }
    }

    private String constructScenarioStatusTable(final String title,
                                                 final Color color,
                                                 final Map<String, String> entries) {
        DynamicTableBuilder tableBuilder = TableBuilder.grid(title)
                .columns("Scenario", "Reason");
        entries.forEach(tableBuilder::row);
        return tableBuilder
                .color(color)
                .align(Align.LEFT)
                .build();
    }
}
