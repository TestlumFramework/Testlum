package com.knubisoft.e2e.testing.framework.util;

import com.amazonaws.services.simpleemail.model.Message;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.model.ScenarioArguments;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.model.scenario.CommandWithLocator;
import com.knubisoft.e2e.testing.model.scenario.Overview;
import com.knubisoft.e2e.testing.model.scenario.OverviewPart;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.BROWSER_VERSION_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.COMMENT_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.LOCATOR_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.OVERVIEW_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCENARIO_NUMBER_AND_PATH_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SUBSTEP_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.TESTS_RUN_FAILED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.VARIATION_LOG;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

@UtilityClass
@Slf4j
public class LogUtil {

    private static final String REGEX_NEW_LINE = "[\\r\\n]";

    public void logOverview(final ScenarioArguments scenarioArguments,
                            final AtomicInteger atomicInteger) {
        Overview overview = scenarioArguments.getScenario().getOverview();
        log.info(EMPTY);
        log.info(SCENARIO_NUMBER_AND_PATH_LOG, atomicInteger, overview.getName(),
                scenarioArguments.getFile().getAbsolutePath());
        log.info(OVERVIEW_LOG);
        logOverviewPartInfo(OverviewPart.DESCRIPTION, overview.getDescription());
        logOverviewPartInfo(OverviewPart.JIRA, overview.getJira());
        logOverviewPartInfo(OverviewPart.DEVELOPER, overview.getDeveloper());
        logUiInfo(scenarioArguments);
    }

    public void logAllQueries(final List<String> queries) {
        queries.forEach(query -> log.info(
                format(LogMessage.QUERY_LOG_TEMPLATE, EMPTY, query.replaceAll("\\s{2,}", SPACE))));
    }

    public void logBrokerActionInfo(final String action, final String queue, final String content) {
        log.info(LogMessage.BROKER_ACTION_INFO_LOG,
                action.toUpperCase(Locale.ROOT),
                queue,
                content.replaceAll(REGEX_NEW_LINE, format("%n%-19s", EMPTY)));
    }

    public void logS3ActionInfo(final String action, final String bucket, final String key, final String fileName) {
        log.info(LogMessage.S3_ACTION_INFO_LOG, action.toUpperCase(Locale.ROOT), bucket, key, fileName);
    }

    public void logSESMessage(final Message sesMessage) {
        StringBuilder message = new StringBuilder("Body:");
        if (sesMessage.getBody() != null) {
            appendBodyContentIfNotBlank(sesMessage.getBody().getHtml().getData(), "HTML", message);
            appendBodyContentIfNotBlank(sesMessage.getBody().getText().getData(), "Text", message);
        } else {
            message.append("Message body is empty");
        }
        log.info(message.toString());
    }

    private void appendBodyContentIfNotBlank(final String data, final String title, final StringBuilder sb) {
        if (StringUtils.isNotBlank(data)) {
            sb.append(format(LogMessage.SES_BODY_CONTENT_AND_TITLE_TEMPLATE,
                    title,
                    EMPTY,
                    data.replaceAll(REGEX_NEW_LINE, format("%n%15s", EMPTY))));
        }
    }

    private void logOverviewPartInfo(final OverviewPart part, final String data) {
        if (StringUtils.isNotBlank(data)) {
            log.info(LogMessage.OVERVIEW_INFO_LOG, part.getPartTitle(), data);
        }
    }

    private void logUiInfo(final ScenarioArguments scenarioArguments) {
        if (scenarioArguments.isContainsUiSteps()) {
            if (StringUtils.isNotBlank(scenarioArguments.getScenario().getVariations())) {
                log.info(VARIATION_LOG, scenarioArguments.getScenario().getVariations());
            }
            log.info(BROWSER_VERSION_LOG, scenarioArguments.getBrowserVersion());
        }
    }

    public void logTestExecutionSummary(final TestExecutionSummary testExecutionSummary) {
        if (testExecutionSummary.getTestsFoundCount() == 0 && !testExecutionSummary.getFailures().isEmpty()) {
            testExecutionSummary.getFailures().forEach(e -> log.error(TESTS_RUN_FAILED, e.getException()));
        } else {
            logTestsStatistics(testExecutionSummary);
        }
    }

    private void logTestsStatistics(final TestExecutionSummary testExecutionSummary) {
        long failedScenarios = testExecutionSummary.getTestsFailedCount();
        log.info(LogMessage.TEST_EXECUTION_SUMMARY_TEMPLATE,
                testExecutionSummary.getTestsFoundCount(),
                testExecutionSummary.getTestsSkippedCount(),
                testExecutionSummary.getTestsStartedCount(),
                testExecutionSummary.getTestsAbortedCount(),
                testExecutionSummary.getTestsSucceededCount(),
                failedScenarios);
        if (failedScenarios > 0) {
            testExecutionSummary.getFailures().forEach(e -> log.error(format(LogMessage.FAILED_SCENARIOS_NAME_TEMPLATE,
                            e.getTestIdentifier().getDisplayName()), e.getException()));
        }
    }

    public void logSubstep(final InterpreterDependencies dependencies, final AbstractCommand action) {
        log.info(SUBSTEP_LOG, dependencies.getPosition().getAndIncrement(), action.getClass().getSimpleName());
        log.info(COMMENT_LOG, action.getComment());
        if (action instanceof CommandWithLocator) {
            log.info(LOCATOR_LOG, ((CommandWithLocator) action).getLocatorId());
        }
    }

}
