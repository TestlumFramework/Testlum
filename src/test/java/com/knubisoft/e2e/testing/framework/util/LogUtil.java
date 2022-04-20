package com.knubisoft.e2e.testing.framework.util;

import com.amazonaws.services.simpleemail.model.Message;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

@UtilityClass
@Slf4j
public class LogUtil {

    private static final String REGEX_NEW_LINE = "[\\r\\n]";

//    public void logScenarioOverview(final Overview overview) {
//        logOverviewPartInfo(OverviewPart.NAME, overview.getName());
//        logOverviewPartInfo(OverviewPart.DESCRIPTION, overview.getDescription());
//        logOverviewPartInfo(OverviewPart.JIRA, overview.getJira());
//        logOverviewPartInfo(OverviewPart.DEVELOPER, overview.getDeveloper());
//    }

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

//    private void logOverviewPartInfo(final OverviewPart part, final String data) {
//        if (StringUtils.isNotBlank(data)) {
//            log.info(LogMessage.OVERVIEW_INFO_LOG, part.getPartTitle(), data);
//        }
//    }

    public void logTestExecutionSummary(final TestExecutionSummary testExecutionSummary) {
        long failedScenarios = testExecutionSummary.getTestsFailedCount();
        log.info(LogMessage.TEST_EXECUTION_SUMMARY_TEMPLATE,
                testExecutionSummary.getTestsFoundCount(),
                testExecutionSummary.getTestsSkippedCount(),
                testExecutionSummary.getTestsStartedCount(),
                testExecutionSummary.getTestsAbortedCount(),
                testExecutionSummary.getTestsSucceededCount(),
                failedScenarios);
        if (failedScenarios > 0) {
            logFailedScenariosInfo(testExecutionSummary.getFailures());
        }
    }

    private void logFailedScenariosInfo(final List<TestExecutionSummary.Failure> failures) {
        for (TestExecutionSummary.Failure failureScenario : failures) {
            log.error(format(LogMessage.FAILED_SCENARIOS_NAME_TEMPLATE,
                    failureScenario.getTestIdentifier().getDisplayName()),
                    failureScenario.getException());
        }
    }
}
