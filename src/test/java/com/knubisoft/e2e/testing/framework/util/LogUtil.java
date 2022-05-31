package com.knubisoft.e2e.testing.framework.util;

import com.amazonaws.services.simpleemail.model.Message;
import com.knubisoft.e2e.testing.model.ScenarioArguments;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.model.scenario.CommandWithLocator;
import com.knubisoft.e2e.testing.model.scenario.Overview;
import com.knubisoft.e2e.testing.model.scenario.OverviewPart;
import com.knubisoft.e2e.testing.model.scenario.Ses;
import com.knubisoft.e2e.testing.model.scenario.Ui;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.BODY_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.BROWSER_VERSION_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.COMMENT_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.DESTINATION_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ENDPOINT_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.EXECUTION_TIME_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.HTTP_METHOD_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.LOCATOR_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAME_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCENARIO_NUMBER_AND_PATH_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SOURCE_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.UI_COMMAND_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.TABLE_FORMAT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.TESTS_RUN_FAILED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.UI_EXECUTION_TIME_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.VALUE_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.VARIATION_LOG;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

@UtilityClass
@Slf4j
public class LogUtil {

    private static final String REGEX_NEW_LINE = "[\\r\\n]";

    public void logScenarioDetails(final ScenarioArguments scenarioArguments,
                                   final AtomicInteger atomicInteger) {
        Overview overview = scenarioArguments.getScenario().getOverview();
        log.info(EMPTY);
        log.info(SCENARIO_NUMBER_AND_PATH_LOG, atomicInteger,
                scenarioArguments.getFile().getAbsolutePath());
        logOverview(overview);
        if (scenarioArguments.isContainsUiSteps()) {
            logUiInfo(scenarioArguments.getScenario().getVariations(), scenarioArguments.getBrowserVersion());
        }
    }

    private void logOverview(final Overview overview) {
        logOverviewPartInfo(OverviewPart.NAME, overview.getName());
        logOverviewPartInfo(OverviewPart.DESCRIPTION, overview.getDescription());
        logOverviewPartInfo(OverviewPart.JIRA, overview.getJira());
        logOverviewPartInfo(OverviewPart.DEVELOPER, overview.getDeveloper());
        overview.getLink().forEach(link -> logOverviewPartInfo(OverviewPart.LINK, link));
    }

    public void logAllQueries(final List<String> queries, final String alias, final String url) {
        log.info(ALIAS_LOG, alias, url);
        queries.forEach(query -> log.info(
                format(TABLE_FORMAT, "Query", query.replaceAll("\\s{2,}", SPACE))));
    }

    public void logBrokerActionInfo(final String action, final String queue, final String content) {
        log.info(LogMessage.BROKER_ACTION_INFO_LOG,
                action.toUpperCase(Locale.ROOT),
                queue,
                StringUtils.isNotBlank(content)
                        ? PrettifyStringJson.getJSONResult(content)
                                .replaceAll(REGEX_NEW_LINE, format("%n%-41s|", EMPTY)) : content);
    }

    public void logS3ActionInfo(final String action, final String bucket, final String key, final String fileName) {
        log.info(LogMessage.S3_ACTION_INFO_LOG, action.toUpperCase(Locale.ROOT), bucket, key, fileName);
    }

    public void logSESMessage(final Message sesMessage) {
        StringBuilder message = new StringBuilder();
        if (sesMessage.getBody() != null) {
            appendBodyContentIfNotBlank(sesMessage.getBody().getHtml().getData(), "HTML", message);
            appendBodyContentIfNotBlank(sesMessage.getBody().getText().getData(), "Text", message);
        } else {
            message.append("Message body is empty");
        }
        log.info(BODY_LOG, message);
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

    private void logUiInfo(final String variation, final String browserVersion) {
        if (StringUtils.isNotBlank(variation)) {
            log.info(VARIATION_LOG, variation);
        }
        log.info(BROWSER_VERSION_LOG, browserVersion);
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

    public void logUICommand(final int position, final AbstractCommand action) {
        log.info(UI_COMMAND_LOG, position, action.getClass().getSimpleName());
        log.info(COMMENT_LOG, action.getComment());
        if (action instanceof CommandWithLocator) {
            log.info(LOCATOR_LOG, ((CommandWithLocator) action).getLocatorId());
        }
    }

    public void logSesInfo(final Ses ses, final String url) {
        log.info(ALIAS_LOG, ses.getAlias(), url);
        log.info(SOURCE_LOG, ses.getSource());
        log.info(DESTINATION_LOG, ses.getDestination());
    }

    public void logVarInfo(final String name, final String value) {
        log.info(NAME_LOG, name);
        log.info(VALUE_LOG, value);
    }

    public void logHttpInfo(final String alias, final String method, final String endpoint, final String url) {
        log.info(ALIAS_LOG, alias, url);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);

    }

    public void logBody(final String body) {
        if (StringUtils.isNotBlank(body)) {
            log.info(BODY_LOG,
                PrettifyStringJson.getJSONResult(body)
                .replaceAll(REGEX_NEW_LINE, format("%n%-41s|", EMPTY)));
        }
    }

    public void logBodyContent(final HttpEntity body) {
        if (body != null) {
            try {
                logBody(IOUtils.toString(body.getContent(), StandardCharsets.UTF_8.name()));
            } catch (IOException e) {
                log.error("Can`t get HttpEntity content", e);
            }
        }
    }

    public void logExecutionTime(final long time, final AbstractCommand command) {
        if (command instanceof Ui) {
            log.info(UI_EXECUTION_TIME_LOG, time);
        } else {
            log.info(EXECUTION_TIME_LOG, time);
        }
    }

}
