package com.knubisoft.cott.testing.framework.util;

import com.amazonaws.services.simpleemail.model.Message;
import com.knubisoft.cott.testing.framework.constant.LogMessage;
import com.knubisoft.cott.testing.model.ScenarioArguments;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.Auth;
import com.knubisoft.cott.testing.model.scenario.CommandWithLocator;
import com.knubisoft.cott.testing.model.scenario.CompareWith;
import com.knubisoft.cott.testing.model.scenario.Image;
import com.knubisoft.cott.testing.model.scenario.Overview;
import com.knubisoft.cott.testing.model.scenario.OverviewPart;
import com.knubisoft.cott.testing.model.scenario.Ses;
import com.knubisoft.cott.testing.model.scenario.Smtp;
import com.knubisoft.cott.testing.model.scenario.Twilio;
import com.knubisoft.cott.testing.model.scenario.Ui;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.HttpClientErrorException;
import software.amazon.awssdk.http.HttpStatusCode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.TESTS_RUN_FAILED;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.BODY_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.BROWSER_NAME_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.CLEAR_COOKIES_AFTER;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMMAND_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMMENT_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.CONTENT_FORMAT;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.CONTENT_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.CREDENTIALS_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.DESTINATION_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.ENDPOINT_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.EXCEPTION_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.EXECUTION_TIME_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.EXTRACT_THEN_COMPARE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.FROM_PHONE_NUMBER_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.HIGHLIGHT_DIFFERENCE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.HTTP_METHOD_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.IMAGE_COMPARISON_TYPE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.IMAGE_FOR_COMPARISON_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.IMAGE_SOURCE_ATT_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.INITIAL_STRUCTURE_GENERATION_ERROR;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.INITIAL_STRUCTURE_GENERATION_SUCCESS;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.INVALID_CREDENTIALS_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.INVALID_SCENARIO_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.LOCAL_STORAGE_KEY;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.LOCATOR_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.MESSAGE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.NAME_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.NEW_LOG_LINE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.REGEX_NEW_LINE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SCENARIO_NUMBER_AND_PATH_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SCROLL_BY_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SCROLL_DIRECTION_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SCROLL_LOCATOR;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SERVER_BAD_GATEWAY_RESPONSE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SERVER_ERROR_RESPONSE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SMTP_HOST_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SMTP_PORT_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SOURCE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SUBJECT_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.TABLE_FORMAT;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.TAKE_SCREENSHOT_THEN_COMPARE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.TO_PHONE_NUMBER_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.UI_COMMAND_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.UI_EXECUTION_TIME_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.VARIATION_LOG;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

@UtilityClass
@Slf4j
public class LogUtil {

    public void logScenarioDetails(final ScenarioArguments scenarioArguments,
                                   final AtomicInteger atomicInteger) {
        log.info(EMPTY);
        log.info(SCENARIO_NUMBER_AND_PATH_LOG, atomicInteger, scenarioArguments.getFile().getAbsolutePath());
        Overview overview = scenarioArguments.getScenario().getOverview();
        logOverview(overview);
        if (scenarioArguments.isContainsUiSteps()) {
            logUiInfo(scenarioArguments.getScenario().getVariations(),
                    BrowserUtil.getBrowserInfo(scenarioArguments.getBrowser()));
        }

    }

    private void logOverview(final Overview overview) {
        logOverviewPartInfo(OverviewPart.NAME, overview.getName());
        logOverviewPartInfo(OverviewPart.DESCRIPTION, overview.getDescription());
        logOverviewPartInfo(OverviewPart.JIRA, overview.getJira());
        logOverviewPartInfo(OverviewPart.DEVELOPER, overview.getDeveloper());
        overview.getLink().forEach(link -> logOverviewPartInfo(OverviewPart.LINK, link));
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
                StringUtils.isNotBlank(content)
                        ? PrettifyStringJson.getJSONResult(content)
                                .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT) : content);
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
        log.info(BROWSER_NAME_LOG, browserVersion);
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

    public void logSesInfo(final Ses ses) {
        log.info(ALIAS_LOG, ses.getAlias());
        log.info(SOURCE_LOG, ses.getSource());
        log.info(DESTINATION_LOG, ses.getDestination());
    }

    public void logVarInfo(final String name, final String value) {
        log.info(NAME_LOG, name);
        log.info(VALUE_LOG, value);
    }

    public void logHttpInfo(final String alias, final String method, final String endpoint) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);

    }

    public void logBody(final String body) {
        if (StringUtils.isNotBlank(body)) {
            log.info(BODY_LOG,
                PrettifyStringJson.getJSONResult(body)
                .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    @SneakyThrows
    public void logBodyContent(final HttpEntity body) {
        if (body != null) {
            logBody(IOUtils.toString(body.getContent(), StandardCharsets.UTF_8.name()));
        }
    }

    public void logExecutionTime(final long time, final AbstractCommand command) {
        if (command instanceof Ui) {
            log.info(UI_EXECUTION_TIME_LOG, time);
        } else {
            log.info(EXECUTION_TIME_LOG, time);
        }
    }

    public void logScrollInfo(final String direction, final String measure, final String value) {
        log.info(SCROLL_DIRECTION_LOG, direction);
        log.info(SCROLL_BY_LOG, measure);
        log.info(VALUE_LOG, value);
    }

    public void logInnerScrollInfo(
            final String direction, final String measure, final String value, final String selector) {
        logScrollInfo(direction, measure, value);
        log.info(SCROLL_LOCATOR, selector);
    }

    public void logNonParsedScenarioInfo(final String path, final String exception) {
        log.error(INVALID_SCENARIO_LOG, path, exception);
    }

    public void logUiAttributes(final boolean isClearCookies, final String storageKey) {
        log.info(CLEAR_COOKIES_AFTER, isClearCookies);
        if (StringUtils.isNotEmpty(storageKey)) {
            log.info(LOCAL_STORAGE_KEY, storageKey);
        }
    }

    public void logAuthInfo(final Auth auth) {
        log.info(ALIAS_LOG, auth.getApiAlias());
        log.info(ENDPOINT_LOG, auth.getLoginEndpoint());
        log.info(CREDENTIALS_LOG, auth.getCredentials());
    }

    public void logResponseStatusError(final HttpClientErrorException exception) {
        if (HttpStatusCode.NOT_FOUND == exception.getRawStatusCode()) {
            log.info(INVALID_CREDENTIALS_LOG, exception.getRawStatusCode());
        } else if (HttpStatusCode.BAD_GATEWAY == exception.getRawStatusCode()) {
            log.info(SERVER_BAD_GATEWAY_RESPONSE_LOG, exception.getRawStatusCode());
        } else {
            log.info(SERVER_ERROR_RESPONSE_LOG, exception.getRawStatusCode());
        }
    }

    public void logException(final Exception ex) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    public void logHover(final int position, final CommandWithLocator action) {
        log.info(COMMAND_LOG, position, action.getClass().getSimpleName());
        log.info(COMMENT_LOG, action.getComment());
        log.info(LOCATOR_LOG, action.getLocatorId());
    }

    public void logSmtpInfo(final Smtp smtp, final JavaMailSenderImpl javaMailSender) {
        log.info(ALIAS_LOG, smtp.getAlias());
        log.info(SMTP_HOST_LOG, javaMailSender.getHost());
        log.info(SMTP_PORT_LOG, javaMailSender.getPort());
        log.info(SOURCE_LOG, javaMailSender.getUsername());
        log.info(DESTINATION_LOG, smtp.getRecipientEmail());
        log.info(SUBJECT_LOG, smtp.getSubject());
        log.info(CONTENT_LOG, smtp.getText());
    }

    public void logTwilioInfo(final Twilio twilio, final String twilioPhoneNumber) {
        log.info(FROM_PHONE_NUMBER_LOG, twilioPhoneNumber);
        log.info(TO_PHONE_NUMBER_LOG, twilio.getDestinationPhoneNumber());
        log.info(MESSAGE_LOG, twilio.getMessage());
    }

    public static void logImageComparisonInfo(final Image image) {
        log.info(IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        CompareWith compareWith = image.getCompareWith();
        if (Objects.nonNull(compareWith)) {
            log.info(IMAGE_COMPARISON_TYPE_LOG, EXTRACT_THEN_COMPARE);
            log.info(LOCATOR_LOG, compareWith.getLocator());
            log.info(IMAGE_SOURCE_ATT_LOG, compareWith.getAttribute());
        } else {
            log.info(IMAGE_COMPARISON_TYPE_LOG, TAKE_SCREENSHOT_THEN_COMPARE);
        }
    }

    public void logStructureGeneration(final String path) {
        log.info(INITIAL_STRUCTURE_GENERATION_SUCCESS, path);
    }

    public void logErrorStructureGeneration(final String path, final Exception ex) {
        log.error(INITIAL_STRUCTURE_GENERATION_ERROR, path, ex);
    }
}
