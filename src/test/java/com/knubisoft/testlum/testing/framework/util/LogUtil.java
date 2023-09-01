package com.knubisoft.testlum.testing.framework.util;

import com.amazonaws.services.simpleemail.model.Message;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.model.ScenarioArguments;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.AssertTitle;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import com.knubisoft.testlum.testing.model.scenario.CommandWithLocator;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import com.knubisoft.testlum.testing.model.scenario.DragAndDropNative;
import com.knubisoft.testlum.testing.model.scenario.Exclude;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import com.knubisoft.testlum.testing.model.scenario.Hover;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import com.knubisoft.testlum.testing.model.scenario.OverviewPart;
import com.knubisoft.testlum.testing.model.scenario.Part;
import com.knubisoft.testlum.testing.model.scenario.Picture;
import com.knubisoft.testlum.testing.model.scenario.ReceiveKafkaMessage;
import com.knubisoft.testlum.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.testlum.testing.model.scenario.ReceiveSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.RedisQuery;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollNative;
import com.knubisoft.testlum.testing.model.scenario.ScrollType;
import com.knubisoft.testlum.testing.model.scenario.SendKafkaMessage;
import com.knubisoft.testlum.testing.model.scenario.SendRmqMessage;
import com.knubisoft.testlum.testing.model.scenario.SendSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.Ses;
import com.knubisoft.testlum.testing.model.scenario.Smtp;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.Twilio;
import com.knubisoft.testlum.testing.model.scenario.Ui;
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
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ACTION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ATTRIBUTE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.BODY_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.BROWSER_NAME_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CLEAR_COOKIES_AFTER;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_LOG_WITHOUT_POSITION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_SKIPPED_ON_CONDITION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMENT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMIT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONDITION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONTENT_FORMAT;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONTENT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CORRELATION_ID_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CREDENTIALS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.DB_TYPE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.DELAY_SECONDS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.DESTINATION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.DRAGGING_FILE_PATH;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.DRAGGING_FROM;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.DROPPING_TO;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ENDPOINT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.END_UI_COMMANDS_IN_FRAME;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.END_UI_COMMANDS_IN_WEBVIEW;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ERROR_SQL_QUERY;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXCEPTION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXECUTION_TIME_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXPRESSION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXTRACT_THEN_COMPARE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FROM_PHONE_NUMBER_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.HIGHLIGHT_DIFFERENCE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.HOTKEY_COMMAND_TIMES;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.HTTP_METHOD_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.IMAGE_COMPARISON_TYPE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.IMAGE_EXCLUDED_ELEMENT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.IMAGE_FOR_COMPARISON_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.IMAGE_MATCH_PERCENTAGE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.IMAGE_SOURCE_ATT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.INITIAL_STRUCTURE_GENERATION_ERROR;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.INITIAL_STRUCTURE_GENERATION_SUCCESS;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.INVALID_CREDENTIALS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.INVALID_SCENARIO_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.LAMBDA_FUNCTION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.LAMBDA_PAYLOAD_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.LOCAL_STORAGE_KEY;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.LOCATOR_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MAX_NUMBER_OF_MESSAGES_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MESSAGE_DEDUPLICATION_ID_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MESSAGE_GROUP_ID_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MESSAGE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MOBILEBROWSER_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.MOVE_TO_EMPTY_SPACE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.NAME_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.NATIVE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.NEW_LOG_LINE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.POSITION_COMMAND_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.PREFETCH_COUNT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.QUERY;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.QUEUE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.RECEIVE_REQUEST_ATTEMPT_ID_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.REDIS_QUERY;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.REGEX_NEW_LINE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ROUTING_KEY_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SCENARIO_NUMBER_AND_PATH_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SCROLL_BY_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SCROLL_DIRECTION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SCROLL_LOCATOR;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SCROLL_TYPE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SCROLL_VALUE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SERVER_BAD_GATEWAY_RESPONSE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SERVER_ERROR_RESPONSE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SKIPPED_BODY_VALIDATION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SMTP_HOST_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SMTP_PORT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SOURCE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.START_UI_COMMANDS_IN_FRAME;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.START_UI_COMMANDS_IN_WEBVIEW;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SUBJECT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SWIPE_DIRECTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SWIPE_QUANTITY;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SWIPE_TYPE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SWIPE_VALUE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TAB_COMMAND;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TAB_INDEX;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TAB_URL;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TAKE_SCREENSHOT_THEN_COMPARE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TESTS_RUN_FAILED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TIMEOUT_MILLIS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TOPIC_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TO_PHONE_NUMBER_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.UI_COMMAND_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.UI_COMMAND_LOG_WITHOUT_POSITION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.UI_EXECUTION_TIME_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VARIATION_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VISIBILITY_TIMEOUT_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_TIME_SECONDS_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LAST_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.OPEN_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.WITHOUT_URL;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
@Slf4j
public class LogUtil {

    private static final int MAX_CONTENT_LENGTH = 25 * 1024;

    /* execution log */
    public void logScenarioDetails(final ScenarioArguments scenarioArguments, final int scenarioId) {
        log.info(EMPTY);
        log.info(SCENARIO_NUMBER_AND_PATH_LOG, scenarioId, scenarioArguments.getFile().getAbsolutePath());
        Overview overview = scenarioArguments.getScenario().getOverview();
        logOverview(overview);
        if (scenarioArguments.isContainsUiSteps()) {
            logUiInfo(scenarioArguments.getScenario().getSettings().getVariations(),
                    scenarioArguments.getEnvironment(),
                    scenarioArguments.getBrowser(),
                    scenarioArguments.getMobilebrowserDevice(),
                    scenarioArguments.getNativeDevice());
        }
    }

    private void logOverview(final Overview overview) {
        logOverviewPartInfo(OverviewPart.NAME, overview.getName());
        logOverviewPartInfo(OverviewPart.DESCRIPTION, overview.getDescription());
        logOverviewPartInfo(OverviewPart.JIRA, overview.getJira());
        logOverviewPartInfo(OverviewPart.DEVELOPER, overview.getDeveloper());
        overview.getLink().forEach(link -> logOverviewPartInfo(OverviewPart.LINK, link));
    }

    private void logOverviewPartInfo(final OverviewPart overviewPart, final String data) {
        if (isNotBlank(data)) {
            log.info(LogMessage.OVERVIEW_INFO_LOG, overviewPart.value(), data);
        }
    }

    private void logUiInfo(final String variation,
                           final String environment,
                           final String browserAlias,
                           final String mobilebrowserAlias,
                           final String nativeDeviceAlias) {
        if (isNotBlank(variation)) {
            log.info(VARIATION_LOG, variation);
        }
        BrowserUtil.getBrowserBy(environment, browserAlias).ifPresent(abstractBrowser ->
                log.info(BROWSER_NAME_LOG, BrowserUtil.getBrowserInfo(abstractBrowser)));

        MobileUtil.getMobilebrowserDeviceBy(environment, mobilebrowserAlias).ifPresent(mobilebrowserDevice ->
                log.info(MOBILEBROWSER_LOG, MobileUtil.getMobilebrowserDeviceInfo(mobilebrowserDevice)));

        MobileUtil.getNativeDeviceBy(environment, nativeDeviceAlias).ifPresent(nativeDevice ->
                log.info(NATIVE_LOG, MobileUtil.getNativeDeviceInfo(nativeDevice)));
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
            testExecutionSummary.getFailures().forEach(e -> log.error(
                    format(LogMessage.FAILED_SCENARIOS_NAME_TEMPLATE, e.getTestIdentifier().getDisplayName()),
                    e.getException()));
        }
    }

    public void logNonParsedScenarioInfo(final String path, final String exception) {
        log.error(INVALID_SCENARIO_LOG, path, exception);
    }

    /* general log */
    public void logCommand(final long position, final AbstractCommand command) {
        if (position != 0) {
            log.info(POSITION_COMMAND_LOG, position, command.getClass().getSimpleName());
        } else {
            log.info(COMMAND_LOG_WITHOUT_POSITION, command.getClass().getSimpleName());
        }
    }

    public void logAlias(final String alias) {
        log.info(ALIAS_LOG, alias);
    }

    public void logCondition(final String name, final boolean condition) {
        if (!condition) {
            log.info(COMMAND_SKIPPED_ON_CONDITION_LOG);
        }
        log.info(CONDITION_LOG, name, condition);
    }

    public void logConditionInfo(final String name,
                                 final String expression,
                                 final boolean value) {
        log.info(NAME_LOG, name);
        log.info(EXPRESSION_LOG, expression);
        log.info(VALUE_LOG, value);
    }

    public void logExecutionTime(final long time, final AbstractCommand command) {
        if (Ui.class.isAssignableFrom(command.getClass())) {
            log.info(UI_EXECUTION_TIME_LOG, time);
        } else {
            log.info(EXECUTION_TIME_LOG, time);
        }
    }

    public void logException(final Exception ex) {
        if (isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    public void logError(final Exception ex) {
        log.error(LogMessage.ERROR_LOG, ex);
    }

    public void logStructureGeneration(final String path) {
        log.info(INITIAL_STRUCTURE_GENERATION_SUCCESS, path);
    }

    public void logErrorStructureGeneration(final String path, final Exception ex) {
        log.error(INITIAL_STRUCTURE_GENERATION_ERROR, path, ex);
    }

    /* integrations log */
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

    public void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(QUERY, query.replaceAll(REGEX_MANY_SPACES, SPACE)));
    }

    public void logAllQueries(final String dbType, final List<String> queries, final String alias) {
        log.info(DB_TYPE_LOG, dbType);
        logAllQueries(queries, alias);
    }

    public void logAllRedisQueries(final List<RedisQuery> redisQueries, final String alias) {
        log.info(ALIAS_LOG, alias);
        redisQueries.forEach(query ->
                log.info(REDIS_QUERY, query.getCommand(), String.join(SPACE, query.getArg())));
    }

    public void logSqlException(final Exception ex, final String query) {
        if (isNotBlank(ex.getMessage())) {
            log.error(ERROR_SQL_QUERY, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE),
                    SqlUtil.getBrokenQuery(ex, query).replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(ERROR_SQL_QUERY, ex.toString().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        }
    }

    public void logKafkaSendInfo(final SendKafkaMessage send, final String content) {
        logMessageBrokerGeneralMetaData(SEND_ACTION, TOPIC_LOG, send.getTopic(), content);
        logIfNotNull(CORRELATION_ID_LOG, send.getCorrelationId());
    }

    public void logKafkaReceiveInfo(final ReceiveKafkaMessage receive, final String content) {
        logMessageBrokerGeneralMetaData(RECEIVE_ACTION, TOPIC_LOG, receive.getTopic(), content);
        logIfNotNull(TIMEOUT_MILLIS_LOG, receive.getTimeoutMillis());
        logIfNotNull(COMMIT_LOG, receive.isCommit());
    }

    public void logRabbitSendInfo(final SendRmqMessage send, final String content) {
        logMessageBrokerGeneralMetaData(SEND_ACTION, ROUTING_KEY_LOG, send.getRoutingKey(), content);
        logIfNotNull(CORRELATION_ID_LOG, send.getCorrelationId());
    }

    public void logRabbitReceiveInfo(final ReceiveRmqMessage receive, final String content) {
        logMessageBrokerGeneralMetaData(RECEIVE_ACTION, QUEUE_LOG, receive.getQueue(), content);
        logIfNotNull(TIMEOUT_MILLIS_LOG, receive.getTimeoutMillis());
        logIfNotNull(PREFETCH_COUNT_LOG, receive.getPrefetchCount());
    }

    public void logSQSSendInfo(final SendSqsMessage send, final String content) {
        logMessageBrokerGeneralMetaData(SEND_ACTION, QUEUE_LOG, send.getQueue(), content);
        logIfNotNull(DELAY_SECONDS_LOG, send.getDelaySeconds());
        logIfNotNull(MESSAGE_DEDUPLICATION_ID_LOG, send.getMessageDeduplicationId());
        logIfNotNull(MESSAGE_GROUP_ID_LOG, send.getMessageGroupId());
    }

    public void logSQSReceiveInfo(final ReceiveSqsMessage receive, final String content) {
        logMessageBrokerGeneralMetaData(RECEIVE_ACTION, QUEUE_LOG, receive.getQueue(), content);
        logIfNotNull(MAX_NUMBER_OF_MESSAGES_LOG, receive.getMaxNumberOfMessages());
        logIfNotNull(WAIT_TIME_SECONDS_LOG, receive.getWaitTimeSeconds());
        logIfNotNull(RECEIVE_REQUEST_ATTEMPT_ID_LOG, receive.getReceiveRequestAttemptId());
        logIfNotNull(VISIBILITY_TIMEOUT_LOG, receive.getVisibilityTimeout());
    }

    public void logMessageBrokerGeneralMetaData(final String action,
                                                final String topicOrRoutingKeyOrQueue,
                                                final String topicOrRoutingKeyOrQueueValue,
                                                final String content) {
        log.info(ACTION_LOG, action.toUpperCase(Locale.ROOT));
        log.info(topicOrRoutingKeyOrQueue, topicOrRoutingKeyOrQueueValue);
        log.info(CONTENT_LOG, StringPrettifier.asJsonResult(content.replaceAll(REGEX_MANY_SPACES, SPACE))
                .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
    }

    private void logIfNotNull(final String title, final Object data) {
        if (nonNull(data)) {
            log.info(title, data);
        }
    }

    public void logS3BucketActionInfo(final String action, final String bucket) {
        log.info(LogMessage.S3_BUCKET_ACTION_INFO_LOG, action.toUpperCase(Locale.ROOT), bucket);
    }

    public void logS3FileActionInfo(final String action, final String bucket, final String key) {
        log.info(LogMessage.S3_FILE_ACTION_INFO_LOG, action.toUpperCase(Locale.ROOT), bucket, key);
    }

    public void logSESMessage(final Message sesMessage) {
        StringBuilder message = new StringBuilder();
        if (nonNull(sesMessage.getBody())) {
            appendBodyContentIfNotBlank(sesMessage.getBody().getHtml().getData(), "HTML", message);
            appendBodyContentIfNotBlank(sesMessage.getBody().getText().getData(), "Text", message);
        } else {
            message.append("Message body is empty");
        }
        log.info(BODY_LOG, message);
    }

    private void appendBodyContentIfNotBlank(final String data, final String title, final StringBuilder sb) {
        if (isNotBlank(data)) {
            sb.append(format(LogMessage.SES_BODY_CONTENT_AND_TITLE_TEMPLATE,
                    title,
                    EMPTY,
                    data.replaceAll(REGEX_NEW_LINE, format("%n%15s", EMPTY))));
        }
    }

    public void logSesInfo(final Ses ses) {
        log.info(ALIAS_LOG, ses.getAlias());
        log.info(SOURCE_LOG, ses.getSource());
        log.info(DESTINATION_LOG, ses.getDestination());
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

    public void logWebsocketActionInfo(final String action,
                                       final String comment,
                                       final String destination,
                                       final String content) {
        log.info(LogMessage.WEBSOCKET_ACTION_INFO_LOG, comment, action.toUpperCase(Locale.ROOT));
        if (isNotBlank(destination)) {
            log.info(DESTINATION_LOG, destination);
        }
        if (isNotBlank(content)) {
            log.info(CONTENT_LOG, StringPrettifier.asJsonResult(content).replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    public void logLambdaInfo(final String alias, final String functionName, final String payload) {
        log.info(ALIAS_LOG, alias);
        log.info(LAMBDA_FUNCTION_LOG, functionName);
        if (isNotBlank(payload)) {
            log.info(LAMBDA_PAYLOAD_LOG,
                    StringPrettifier.asJsonResult(payload).replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    public void logHttpInfo(final String alias, final String method, final String endpoint) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);
    }

    @SneakyThrows
    public void logBodyContent(final HttpEntity body) {
        if (nonNull(body) && body.getContentLength() < MAX_CONTENT_LENGTH) {
            logBody(IOUtils.toString(body.getContent(), StandardCharsets.UTF_8.name()));
        }
    }

    public void logBody(final String body) {
        if (isNotBlank(body)) {
            log.info(BODY_LOG,
                    StringPrettifier.asJsonResult(StringPrettifier.cut(body))
                            .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    public void logBodyValidationSkipped() {
        log.info(SKIPPED_BODY_VALIDATION);
    }

    public void logVarInfo(final String name, final String value) {
        log.info(NAME_LOG, name);
        log.info(VALUE_LOG, StringPrettifier.cut(value));
    }

    /* ui log */
    public void logUICommand(final long position, final AbstractCommand action) {
        if (position != 0) {
            log.info(UI_COMMAND_LOG, position, action.getClass().getSimpleName());
        } else {
            log.info(UI_COMMAND_LOG_WITHOUT_POSITION, action.getClass().getSimpleName());
        }
        if (isNotBlank(action.getComment())) {
            log.info(COMMENT_LOG, action.getComment());
        }
        if (action instanceof CommandWithLocator) {
            log.info(LOCATOR_LOG, ((CommandWithLocator) action).getLocatorId());
        }
    }

    public void logUiAttributes(final boolean isClearCookies, final String storageKey) {
        log.info(CLEAR_COOKIES_AFTER, isClearCookies);
        if (isNotBlank(storageKey)) {
            log.info(LOCAL_STORAGE_KEY, storageKey);
        }
    }

    public void startUiCommandsInFrame() {
        log.info(START_UI_COMMANDS_IN_FRAME);
    }

    public void endUiCommandsInFrame() {
        log.info(END_UI_COMMANDS_IN_FRAME);
    }

    public void startUiCommandsInWebView() {
        log.info(START_UI_COMMANDS_IN_WEBVIEW);
    }

    public void endUiCommandsInWebView() {
        log.info(END_UI_COMMANDS_IN_WEBVIEW);
    }

    public void logSubCommand(final int position, final Object action) {
        log.info(COMMAND_LOG, position, action.getClass().getSimpleName());
    }

    public void logImageComparisonInfo(final Image image) {
        log.info(IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (nonNull(image.getPicture())) {
            logCompareWithElementInfo(image.getPicture());
        } else if (nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    public void logImageComparisonInfo(final NativeImage image) {
        log.info(IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (nonNull(image.getPicture())) {
            logCompareWithElementInfo(image.getPicture());
        } else if (nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    private void logCompareWithElementInfo(final Picture element) {
        log.info(IMAGE_COMPARISON_TYPE_LOG, EXTRACT_THEN_COMPARE);
        log.info(LOCATOR_LOG, element.getLocatorId());
        log.info(IMAGE_SOURCE_ATT_LOG, element.getAttribute());
    }

    private void logCompareWithFullscreen(final FullScreen fullScreen) {
        log.info(IMAGE_COMPARISON_TYPE_LOG, TAKE_SCREENSHOT_THEN_COMPARE);
        if (nonNull(fullScreen.getPercentage())) {
            log.info(IMAGE_MATCH_PERCENTAGE_LOG, fullScreen.getPercentage());
        }
        if (!fullScreen.getExclude().isEmpty()) {
            log.info(IMAGE_EXCLUDED_ELEMENT_LOG, StringUtils.join(fullScreen.getExclude().stream()
                    .map(Exclude::getLocatorId)
                    .collect(Collectors.joining(COMMA + SPACE))));
        }
    }

    private void logCompareWithPart(final Part part) {
        log.info(IMAGE_COMPARISON_TYPE_LOG, GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE);
        log.info(LOCATOR_LOG, part.getLocatorId());
        if (nonNull(part.getPercentage())) {
            log.info(IMAGE_MATCH_PERCENTAGE_LOG, part.getPercentage());
        }
    }

    public void logScrollInfo(final Scroll scroll) {
        log.info(SCROLL_DIRECTION_LOG, scroll.getDirection());
        log.info(SCROLL_BY_LOG, scroll.getMeasure());
        log.info(VALUE_LOG, scroll.getValue());
        log.info(SCROLL_TYPE, scroll.getType());
        if (ScrollType.INNER == scroll.getType()) {
            log.info(SCROLL_LOCATOR, scroll.getLocatorId());
        }
    }

    public void logHover(final Hover hover) {
        if (hover.isMoveToEmptySpace()) {
            log.info(MOVE_TO_EMPTY_SPACE, hover.isMoveToEmptySpace());
        }
    }

    public void logHotKeyInfo(final AbstractUiCommand command, final int position) {
        log.info(COMMAND_LOG, position, command.getClass().getSimpleName());
        log.info(COMMENT_LOG, command.getComment());
    }

    public void logSingleKeyCommandTimes(final int times) {
        if (times > 1) {
            log.info(HOTKEY_COMMAND_TIMES, times);
        }
    }

    public void logCloseOrSwitchTabCommand(final String command, final Integer tabNumber) {
        log.info(TAB_COMMAND, command);
        log.info(TAB_INDEX, nonNull(tabNumber) ? tabNumber : LAST_TAB);
    }

    public void logOpenTabCommand(final String url) {
        log.info(TAB_COMMAND, OPEN_TAB);
        log.info(TAB_URL, isNotBlank(url) ? url : WITHOUT_URL);
    }

    public void logAssertCommand(final AbstractCommand command, final int position) {
        log.info(COMMAND_LOG, position, command.getClass().getSimpleName());
        log.info(COMMENT_LOG, command.getComment());
    }

    public void logAssertAttributeInfo(final AssertAttribute attribute) {
        log.info(LOCATOR_LOG, attribute.getLocatorId());
        log.info(ATTRIBUTE_LOG, attribute.getName());
        log.info(CONTENT_LOG, StringPrettifier.cut(attribute.getContent()));
    }

    public void logAssertTitleCommand(final AssertTitle title) {
        log.info(CONTENT_LOG, title.getContent());
    }

    public void logDragAndDropInfo(final DragAndDrop dragAndDrop) {
        if (isNotBlank(dragAndDrop.getFileName())) {
            log.info(DRAGGING_FILE_PATH, dragAndDrop.getFileName());
        } else if (isNotBlank(dragAndDrop.getFromLocatorId())) {
            log.info(DRAGGING_FROM, dragAndDrop.getFromLocatorId());
        }
        log.info(DROPPING_TO, dragAndDrop.getToLocatorId());
    }

    public void logDragAndDropNativeInfo(final DragAndDropNative dragAndDropNative) {
        log.info(DRAGGING_FROM, dragAndDropNative.getFromLocatorId());
        log.info(DROPPING_TO, dragAndDropNative.getToLocatorId());
    }

    public void logSwipeNativeInfo(final SwipeNative swipeNative) {
        log.info(SWIPE_TYPE, swipeNative.getType());
        log.info(SWIPE_QUANTITY, swipeNative.getQuantity());
        log.info(SWIPE_DIRECTION, swipeNative.getDirection());
        log.info(SWIPE_VALUE, swipeNative.getPercent());
        if (isNotBlank(swipeNative.getLocatorId())) {
            log.info(LOCATOR_LOG, swipeNative.getLocatorId());
        }
    }

    public void logScrollNativeInfo(final ScrollNative scrollNative) {
        log.info(SCROLL_TYPE, scrollNative.getType());
        log.info(SCROLL_DIRECTION_LOG, scrollNative.getDirection());
        log.info(SCROLL_VALUE, scrollNative.getValue());
        if (isNotBlank(scrollNative.getLocatorId())) {
            log.info(SCROLL_LOCATOR, scrollNative.getLocatorId());
        }
    }
}
