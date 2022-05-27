package com.knubisoft.e2e.testing.framework.util;

import com.amazonaws.services.simpleemail.model.Message;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.model.ScenarioArguments;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.model.scenario.CommandWithLocator;
import com.knubisoft.e2e.testing.model.scenario.Overview;
import com.knubisoft.e2e.testing.model.scenario.OverviewPart;
import com.knubisoft.e2e.testing.model.scenario.Ses;
import com.knubisoft.e2e.testing.model.scenario.Var;
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
import static com.knubisoft.e2e.testing.framework.util.LogMessage.BY_URL_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.COMMENT_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.DESTINATION_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.HTTP_METHOD_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.LOCATOR_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAME_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.OVERVIEW_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCENARIO_NUMBER_AND_PATH_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SOURCE_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SUBSTEP_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.TABLE_FORMAT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.TESTS_RUN_FAILED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.VALUE_LOG;
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
        for (String link: overview.getLink()) {
            logOverviewPartInfo(OverviewPart.LINK, link);
        }
        logUiInfo(scenarioArguments);
    }

    public void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(
                format(TABLE_FORMAT, "Query", query.replaceAll("\\s{2,}", SPACE))));
    }

    public void logBrokerActionInfo(final String action, final String queue, final String content) {
        log.info(LogMessage.BROKER_ACTION_INFO_LOG,
                action.toUpperCase(Locale.ROOT),
                queue,
                content.replaceAll(REGEX_NEW_LINE, format("%n%-41s|", EMPTY)));
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
        log.info(BODY_LOG, message.toString());
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
        log.info(SUBSTEP_LOG, dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
        log.info(COMMENT_LOG, action.getComment());
        if (action instanceof CommandWithLocator) {
            log.info(LOCATOR_LOG, ((CommandWithLocator) action).getLocatorId());
        }
    }

    public void logSesInfo(final Ses ses) {
        log.info(ALIAS_LOG, ses.getAlias());
        log.info(SOURCE_LOG, ses.getSource());
        log.info(DESTINATION_LOG, ses.getDestination());
    }

    public void logVarInfo(final Var var, final String value) {
        log.info(NAME_LOG, var.getName());
        log.info(VALUE_LOG, value);
    }

    public void logHttpInfo(final String alias, final String method, final String url) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(BY_URL_LOG, url);

    }

    public void logBody(final String body) {
        log.info(BODY_LOG, body.replaceAll(REGEX_NEW_LINE, format("%n%-41s|", EMPTY)));
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

}
