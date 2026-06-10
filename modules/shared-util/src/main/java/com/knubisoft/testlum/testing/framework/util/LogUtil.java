package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.log.table.Align;
import com.knubisoft.testlum.log.table.DynamicTableBuilder;
import com.knubisoft.testlum.log.table.TableBuilder;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Ui;
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

    public void logInvalidScenariosSummary(final Map<String, String> warnings, final Map<String, String> errors) {
        if (!errors.isEmpty()) {
            log.error(constructInvalidScenarioTable(LogMessage.FAILED_SCENARIOS_TITLE, Color.RED, errors));
        }
        if (!warnings.isEmpty()) {
            log.warn(constructInvalidScenarioTable(LogMessage.SKIPPED_SCENARIOS_TITLE, Color.YELLOW, warnings));
        }
    }

    private String constructInvalidScenarioTable(final String title,
                                                 final Color color,
                                                 final Map<String, String> entries) {
        DynamicTableBuilder tableBuilder = TableBuilder.grid(title)
                .columns("Scenario", "Reason");
        entries.forEach(tableBuilder::row);
        return tableBuilder
                .color(color)
                .align(Align.CENTER)
                .build();
    }
}
