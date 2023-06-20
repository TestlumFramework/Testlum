package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Attribute;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import com.knubisoft.testlum.testing.model.scenario.CompareWith;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import com.knubisoft.testlum.testing.model.scenario.DragAndDropNative;
import com.knubisoft.testlum.testing.model.scenario.Hovers;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.KafkaHeaders;
import com.knubisoft.testlum.testing.model.scenario.ReceiveKafkaMessage;
import com.knubisoft.testlum.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.testlum.testing.model.scenario.ReceiveSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.RmqHeaders;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollNative;
import com.knubisoft.testlum.testing.model.scenario.ScrollType;
import com.knubisoft.testlum.testing.model.scenario.SendKafkaMessage;
import com.knubisoft.testlum.testing.model.scenario.SendRmqMessage;
import com.knubisoft.testlum.testing.model.scenario.SendSqsMessage;
import com.knubisoft.testlum.testing.model.scenario.Ses;
import com.knubisoft.testlum.testing.model.scenario.SesBody;
import com.knubisoft.testlum.testing.model.scenario.SesMessage;
import com.knubisoft.testlum.testing.model.scenario.Smtp;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.Twilio;
import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSubscribe;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXTRACT_THEN_COMPARE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TAKE_SCREENSHOT_THEN_COMPARE;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
public class ResultUtil {

    public static final String ALIAS = "Alias";
    public static final String API_ALIAS = "API alias";
    public static final String AUTHENTICATION_TYPE = "Authentication type";
    public static final String CREDENTIALS_FILE = "Credentials file";
    public static final String QUEUE = "Queue";
    public static final String MESSAGE_TO_SEND = "Message to send";
    public static final String CONTENT_TO_SEND = "Content to send";
    public static final String EXPECTED_CODE = "Expected code";
    public static final String ACTUAL_CODE = "Actual code";
    public static final String JSON_PATH = "JSON path";
    public static final String XML_PATH = "Xml path";
    public static final String RELATIONAL_DB_QUERY = "Relational DB query";
    public static final String FILE = "File";
    public static final String EXPRESSION = "Expression";
    public static final String CONSTANT = "Constant";
    public static final String NO_EXPRESSION = "No expression";
    public static final String COOKIES = "Cookies";
    public static final String URL = "Url";
    public static final String HTML_DOM = "HTML Dom";
    public static final String FULL_DOM = "Full Dom";
    public static final String LOCATOR_ID = "Locator ID = %s";
    public static final String ELEMENT_PRESENT = "Is the web element present";
    public static final String CONDITION = "Condition";
    public static final String GENERATED_STRING = "Randomly generated string";
    public static final String ASSERT_LOCATOR = "Locator for assert command";
    public static final String ASSERT_ATTRIBUTE = "Assert command attribute";
    public static final String CLICK_LOCATOR = "Locator for click command";
    public static final String INPUT_LOCATOR = "Locator for input command";
    public static final String CLEAR_LOCATOR = "Locator for clear command";
    public static final String SCROLL_LOCATOR = "Locator for scroll-to command";
    public static final String SWITCH_LOCATOR = "Locator for switch command";
    public static final String HOTKEY_LOCATOR = "Locator for hotkey command";
    public static final String INPUT_VALUE = "Value for input";
    public static final String CLICK_METHOD = "Click method";
    public static final String CLOSE_COMMAND = "Close command for";
    public static final String LAST_TAB = "Last recently opened tab";
    public static final String TAB_NUMBER = "Tab with number '%s'";
    public static final String JS_FILE = "JS file to execute";
    public static final String NAVIGATE_TYPE = "Navigate command type";
    public static final String NAVIGATE_URL = "URL for navigate";
    public static final String DROP_DOWN_LOCATOR = "Locator for drop down command";
    public static final String DROP_DOWN_FOR = "Drop down command for";
    public static final String DROP_DOWN_BY = "Process by";
    public static final String NATIVE_NAVIGATE_TO = "Navigate to destination";
    public static final String ALL_VALUES_DESELECT = "all values (deselect)";
    public static final String ONE_VALUE_TEMPLATE = "one value (%s)";
    public static final String CLEAR_COOKIES_AFTER_EXECUTION = "Clear cookies after execution";
    public static final String CLEAR_LOCAL_STORAGE_BY_KEY = "Clear local storage by key";
    public static final String URL_TO_ACTUAL_IMAGE = "URL to actual image";
    public static final String ADDITIONAL_INFO = "Additional info";
    public static final String IMAGE_ATTACHED_TO_STEP = "Actual image attached to report step";
    public static final String SCROLL_TO_ELEMENT = "Scrolling to element with locator id";
    private static final String FROM_LOCATOR = "From element with locator";
    private static final String FROM_LOCAL_FILE = "From local file";
    private static final String TO_LOCATOR = "To element with locator";
    private static final String PERFORM_SWIPE = "Perform swipe with direction";
    private static final String SWIPE_VALUE = "Swipe value in percent due to screen dimensions";
    private static final String SWIPE_QUANTITY = "Quantity of swipes";
    private static final String SWIPE_TYPE = "Swipe type";
    private static final String SWIPE_LOCATOR = "Locator for swipe";
    private static final String SCROLL_DIRECTION = "Scroll direction";
    private static final String SCROLL_MEASURE = "Scroll measure";
    private static final String SCROLL_TYPE = "Scroll type";
    private static final String LOCATOR_FOR_SCROLL = "Locator for scroll";
    private static final String DESTINATION = "Destination";
    private static final String SUBJECT = "Subject";
    private static final String HTML = "HTML";
    private static final String TEXT = "Text";
    private static final String SOURCE = "Source";
    private static final String QUERIES = "Queries";
    private static final String ENABLE = "Enable";
    private static final String DISABLE = "Disable";
    private static final String ENDPOINT = "Endpoint";
    private static final String HTTP_METHOD = "HTTP method";
    private static final String LAMBDA_FUNCTION_NAME = "Function name";
    private static final String LAMBDA_PAYLOAD = "Payload";
    private static final String HEADERS_STATUS = "Headers status";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String TOPIC = "Topic";
    private static final String NUMBER_OF_MESSAGES = "Number of messages";
    private static final String ALL_AVAILABLE_MESSAGES = "All available messages";
    private static final String ROUTING_KEY = "Routing Key";
    private static final String EXCHANGE = "Exchange";
    private static final String ACTION = "Action";
    private static final String SEND = "Send";
    private static final String SQS_DELAY_SECONDS = "Delay";
    private static final String SQS_MESSAGE_DUPLICATION_ID = "Message duplication id";
    private static final String SQS_MESSAGE_GROUP_ID = "Message group id";
    private static final String SQS_MAX_NUMBER_OF_MESSAGES = "Max number of messages";
    private static final String SQS_VISIBILITY_TIMEOUT = "Visibility timeout";
    private static final String SQS_WAIT_TIME_SECONDS = "Wait time";
    private static final String SQS_RECEIVE_REQUEST_ATTEMPT_ID = "Receive request attempt id";
    private static final String COMMENT_FOR_KAFKA_SEND_ACTION = "Send message to Kafka";
    private static final String COMMENT_FOR_RABBIT_SEND_ACTION = "Send message to RabbitMQ";
    private static final String COMMENT_FOR_SQS_SEND_ACTION = "Send message to SQS";
    private static final String COMMENT_FOR_KAFKA_RECEIVE_ACTION = "Receive message from Kafka";
    private static final String COMMENT_FOR_RABBIT_RECEIVE_ACTION = "Receive message from RabbitMQ";
    private static final String COMMENT_FOR_SQS_RECEIVE_ACTION = "Receive message from SQS";
    private static final String TIMEOUT_MILLIS = "Timeout millis";
    private static final String KEY = "Key";
    private static final String BUCKET = "Bucket";
    private static final String CORRELATION_ID = "Correlation ID";
    private static final String RECEIVE = "Receive";
    private static final String SUBSCRIBE = "Subscribe";
    private static final String DATABASE = "Database";
    private static final String DATABASE_ALIAS = "Database alias";
    private static final String PATCHES = "Patches";
    private static final String SHELL_FILES = "Shell files";
    private static final String SHELL_COMMANDS = "Shell commands";
    private static final String TYPE = "Type";
    private static final String NAME = "Name";
    private static final String VALUE = "Value";
    private static final String TIME = "Time";
    private static final String TIME_UNITE = "Time unit";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String MOVE_TO_EMPTY_SPACE = "Move to empty space after execution";
    private static final String HOVER_NUMBER_TEMPLATE = "Hover #%d";
    private static final String STEP_FAILED = "Step failed";
    private static final String FAILED = "failed";
    private static final String SUCCESSFULLY = "successfully";
    private static final String EXECUTION_RESULT_FILENAME = "scenarios_execution_result.txt";
    private static final String SMTP_HOST = "SMTP Host";
    private static final String SMTP_PORT = "SMTP Port";
    private static final String FROM = "From";
    private static final String TO = "To";
    private static final String MESSAGE = "Message";
    private static final String IMAGE_FOR_COMPARISON = "Image for comparison";
    private static final String HIGHLIGHT_DIFFERENCE = "Highlight difference";
    private static final String IMAGE_COMPARISON_TYPE = "Image comparison type";
    private static final String IMAGE_LOCATOR = "Locator to element with image";
    private static final String IMAGE_SOURCE_ATT = "Image source attribute name";

    public CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }

    public CommandResult newUiCommandResultInstance(final int number, final AbstractCommand command) {
        CommandResult commandResult = newCommandResultInstance(number, command);
        commandResult.setComment(command.getComment());
        return commandResult;
    }

    public void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSuccess())) {
            Exception exception = subCommandsResult
                    .stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    public void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    public void setExpectedActual(final String expected, final String actual, final CommandResult result) {
        result.setExpected(expected);
        result.setActual(actual);
    }

    public void addDatabaseMetaData(final String databaseAlias,
                                    final List<String> queries,
                                    final CommandResult result) {
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(QUERIES, queries);
    }

    public void addMigrateMetaData(final String databaseName,
                                   final String databaseAlias,
                                   final List<String> patches,
                                   final CommandResult result) {
        result.put(DATABASE, databaseName);
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(PATCHES, patches);
    }

    public void addMessageBrokerGeneralMetaData(final String alias,
                                                final String action,
                                                final String destination,
                                                final String destinationValue,
                                                final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        result.put(destination, destinationValue);
    }

    public void addHttpMetaData(final String alias,
                                final String httpMethodName,
                                final Map<String, String> headers,
                                final String endpoint,
                                final CommandResult result) {
        result.put(API_ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    public void addGraphQlMetaData(final String alias,
                                   final HttpMethod httpMethod,
                                   final Map<String, String> headers,
                                   final String endpoint,
                                   final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(HTTP_METHOD, httpMethod);
        result.put(ENDPOINT, endpoint);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    public static void addElasticsearchMetaData(final String alias,
                                                final String httpMethodName,
                                                final Map<String, String> headers,
                                                final String endpoint,
                                                final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }


    public void addSendGridMetaData(final String alias,
                                    final String httpMethodName,
                                    final Map<String, String> headers,
                                    final String endpoint,
                                    final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    private void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    public void addSesMetaData(final Ses ses, final CommandResult result) {
        SesMessage message = ses.getMessage();
        SesBody body = message.getBody();
        result.put(ALIAS, ses.getAlias());
        result.put(DESTINATION, ses.getDestination());
        result.put(SOURCE, ses.getSource());
        result.put(SUBJECT, message.getSubject().getValue());
        result.put(TEXT, body.getText().getValue());
        result.put(HTML, body.getHtml().getValue());
    }

    public static void addSmtpMetaData(final Smtp smtp,
                                       final JavaMailSenderImpl javaMailSender,
                                       final CommandResult result) {
        result.put(ALIAS, smtp.getAlias());
        result.put(SMTP_HOST, javaMailSender.getHost());
        result.put(SMTP_PORT, javaMailSender.getPort());
        result.put(SOURCE, javaMailSender.getUsername());
        result.put(DESTINATION, smtp.getRecipientEmail());
        result.put(SUBJECT, smtp.getSubject());
        result.put(TEXT, smtp.getText());
    }

    public static void addTwilioMetaData(final Twilio twilio, final String twilioNumber, final CommandResult result) {
        result.put(ALIAS, twilio.getAlias());
        result.put(FROM, twilioNumber);
        result.put(TO, twilio.getDestinationPhoneNumber());
        result.put(MESSAGE, twilio.getMessage());
    }

    public void addRabbitMQInfoForSendAction(final SendRmqMessage sendAction,
                                             final String alias,
                                             final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_RABBIT_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, ROUTING_KEY, sendAction.getRoutingKey(), result);
        addRabbitMQAdditionalMetaDataForSendAction(sendAction, result);
    }

    public void addRabbitMQInfoForReceiveAction(final ReceiveRmqMessage receiveAction,
                                                final String alias,
                                                final CommandResult result) {

        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_RABBIT_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, QUEUE, receiveAction.getQueue(), result);
        result.put(HEADERS_STATUS, receiveAction.isHeaders() ? ENABLE : DISABLE);
        result.put(TIMEOUT_MILLIS, receiveAction.getTimeoutMillis());
    }

    public void addKafkaInfoForSendAction(final SendKafkaMessage sendAction,
                                          final String alias,
                                          final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_KAFKA_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, TOPIC, sendAction.getTopic(), result);
        addKafkaAdditionalMetaDataForSendAction(sendAction, result);
    }

    public void addKafkaInfoForReceiveAction(final ReceiveKafkaMessage receiveAction,
                                             final String alias,
                                             final CommandResult result) {
        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_KAFKA_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, TOPIC, receiveAction.getTopic(), result);
        result.put(HEADERS_STATUS, receiveAction.isHeaders() ? ENABLE : DISABLE);
        result.put(TIMEOUT_MILLIS, receiveAction.getTimeoutMillis());
    }

    public void addSqsInfoForSendAction(final SendSqsMessage sendAction,
                                        final String alias,
                                        final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_SQS_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, QUEUE, sendAction.getQueue(), result);
        addSqsAdditionalMetaDataForSendAction(sendAction, result);
    }

    public void addSqsInfoForReceiveAction(final ReceiveSqsMessage receiveAction,
                                           final String alias,
                                           final CommandResult result) {
        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_SQS_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, QUEUE, receiveAction.getQueue(), result);
        addSqsAdditionalMetaDataForReceiveAction(receiveAction, result);
    }

    public void addWebsocketInfoForSendAction(final WebsocketSend sendAction,
                                              final String alias,
                                              final String message,
                                              final CommandResult result) {
        addWebsocketGeneralInfo(SEND, sendAction.getComment(), alias, ENDPOINT, sendAction.getEndpoint(), result);
        result.put(MESSAGE_TO_SEND, message);
    }

    public void addWebsocketInfoForReceiveAction(final WebsocketReceive receiveAction,
                                                 final String alias,
                                                 final CommandResult result) {
        addWebsocketGeneralInfo(RECEIVE, receiveAction.getComment(), alias, TOPIC, receiveAction.getTopic(), result);
        result.put(NUMBER_OF_MESSAGES, nonNull(receiveAction.getLimit())
                ? receiveAction.getLimit().intValue() : ALL_AVAILABLE_MESSAGES);
        result.put(TIMEOUT_MILLIS, receiveAction.getTimeoutMillis());
    }

    public void addWebsocketInfoForSubscribeAction(final WebsocketSubscribe subscribe,
                                                   final String alias,
                                                   final CommandResult result) {
        addWebsocketGeneralInfo(SUBSCRIBE, subscribe.getComment(), alias, TOPIC, subscribe.getTopic(), result);
    }

    private static void addWebsocketGeneralInfo(final String action,
                                                final String comment,
                                                final String alias,
                                                final String destination,
                                                final String destinationValue,
                                                final CommandResult result) {
        result.setCommandKey(action);
        result.setComment(comment);
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        if (isNotBlank(destinationValue)) {
            result.put(destination, destinationValue);
        }
    }

    public void addLambdaGeneralMetaData(final String alias,
                                         final String functionName,
                                         final String payload,
                                         final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(LAMBDA_FUNCTION_NAME, functionName);
        result.put(LAMBDA_PAYLOAD, StringPrettifier.asJsonResult(payload));
    }

    public void addS3GeneralMetaData(final String alias,
                                     final String action,
                                     final String key,
                                     final String bucket,
                                     final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        result.put(BUCKET, bucket);
        result.put(KEY, key);
    }

    public void addShellMetaData(final List<String> shellFiles,
                                 final List<String> shellCommands,
                                 final CommandResult result) {
        if (!shellCommands.isEmpty()) {
            result.put(SHELL_COMMANDS, shellCommands);
        }
        if (!shellFiles.isEmpty()) {
            result.put(SHELL_FILES, shellFiles);
        }
    }

    public void addVariableMetaData(final String type,
                                    final String key,
                                    final String expression,
                                    final String value,
                                    final CommandResult result) {
        result.put(TYPE, type);
        result.put(NAME, key);
        result.put(EXPRESSION, expression);
        result.put(VALUE, value);
    }

    public void addVariableMetaData(final String type,
                                    final String key,
                                    final String format,
                                    final String expression,
                                    final String value,
                                    final CommandResult result) {
        addVariableMetaData(type, key, format(format, expression), value, result);
    }

    public void addConditionMetaData(final String key,
                                     final String expression,
                                     final Boolean value,
                                     final CommandResult result) {
        result.put(NAME, key);
        result.put(EXPRESSION, expression);
        result.put(VALUE, value);
    }

    public void addAuthMetaData(final Auth auth, final CommandResult result) {
        result.put(API_ALIAS, auth.getApiAlias());
        result.put(ENDPOINT, auth.getLoginEndpoint());
        result.put(CREDENTIALS_FILE, auth.getCredentials());
    }

    public void addWaitMetaData(final String time,
                                final TimeUnit unit,
                                final CommandResult result) {
        result.put(TIME, time);
        result.put(TIME_UNITE, unit.name());
    }

    public void addDropDownForOneValueMetaData(final String type,
                                               final String processBy,
                                               final String value,
                                               final CommandResult result) {
        result.put(DROP_DOWN_FOR, format(ONE_VALUE_TEMPLATE, type));
        result.put(DROP_DOWN_BY, processBy);
        result.put(VALUE, value);
    }

    public void addScrollMetaData(final Scroll scroll,
                                  final CommandResult result) {
        result.put(SCROLL_DIRECTION, scroll.getDirection());
        result.put(SCROLL_MEASURE, scroll.getMeasure());
        result.put(VALUE, scroll.getValue());
        result.put(SCROLL_TYPE, scroll.getType());
        if (ScrollType.INNER == scroll.getType()) {
            result.put(LOCATOR_FOR_SCROLL, scroll.getLocatorId());
        }
    }

    public void addScrollNativeMetaDada(final ScrollNative scrollNative,
                                        final CommandResult result) {
        result.put(SCROLL_TYPE, scrollNative.getType());
        if (ScrollType.INNER == scrollNative.getType()) {
            result.put(LOCATOR_FOR_SCROLL, scrollNative.getLocatorId());
        }
        result.put(SCROLL_DIRECTION, scrollNative.getDirection());
        result.put(VALUE, scrollNative.getValue());
    }

    public void addDragAndDropMetaDada(final DragAndDrop dragAndDrop,
                                       final CommandResult result) {
        if (isNotBlank(dragAndDrop.getFileName())) {
            result.put(FROM_LOCAL_FILE, dragAndDrop.getFileName());
        } else if (isNotBlank(dragAndDrop.getFromLocatorId())) {
            result.put(FROM_LOCATOR, dragAndDrop.getFromLocatorId());
        }
        result.put(TO_LOCATOR, dragAndDrop.getToLocatorId());
    }

    public void addDragAndDropNativeMetaDada(final DragAndDropNative dragAndDropNative,
                                             final CommandResult result) {
        result.put(FROM_LOCATOR, dragAndDropNative.getFromLocatorId());
        result.put(TO_LOCATOR, dragAndDropNative.getToLocatorId());
    }

    public void addHoversMetaData(final Hovers hovers, final CommandResult result) {
        result.put(MOVE_TO_EMPTY_SPACE, hovers.isMoveToEmptySpace());
        AtomicInteger number = new AtomicInteger(1);
        hovers.getHover().forEach(hover -> {
            String hoverNumber = format(HOVER_NUMBER_TEMPLATE, number.getAndIncrement());
            result.put(hoverNumber, Arrays.asList(hover.getLocatorId(), hovers.getComment()));
        });
    }

    private void addKafkaAdditionalMetaDataForSendAction(final SendKafkaMessage sendAction,
                                                         final CommandResult result) {
        String key = sendAction.getKey();
        String correlationId = sendAction.getCorrelationId();
        KafkaHeaders kafkaHeaders = sendAction.getHeaders();
        if (isNotBlank(key)) {
            result.put(KEY, key);
        }
        if (isNotBlank(correlationId)) {
            result.put(CORRELATION_ID, correlationId);
        }
        if (nonNull(kafkaHeaders)) {
            result.put(ADDITIONAL_HEADERS, kafkaHeaders.getHeader().stream().map(header ->
                    format(HEADER_TEMPLATE, header.getName(), header.getValue())).collect(Collectors.toList()));
        }
    }

    private void addRabbitMQAdditionalMetaDataForSendAction(final SendRmqMessage sendAction,
                                                            final CommandResult result) {
        String exchange = sendAction.getExchange();
        String correlationId = sendAction.getCorrelationId();
        RmqHeaders rabbitHeaders = sendAction.getHeaders();
        if (isNotBlank(exchange)) {
            result.put(EXCHANGE, exchange);
        }
        if (isNotBlank(correlationId)) {
            result.put(CORRELATION_ID, correlationId);
        }
        if (nonNull(rabbitHeaders)) {
            result.put(ADDITIONAL_HEADERS, rabbitHeaders.getHeader().stream().map(header ->
                    format(HEADER_TEMPLATE, header.getName(), header.getValue())).collect(Collectors.toList()));
        }
    }

    private void addSqsAdditionalMetaDataForSendAction(final SendSqsMessage sendAction, final CommandResult result) {
        if (nonNull(sendAction.getDelaySeconds())) {
            result.put(SQS_DELAY_SECONDS, sendAction.getDelaySeconds());
        }
        if (isNotBlank(sendAction.getMessageDeduplicationId())) {
            result.put(SQS_MESSAGE_DUPLICATION_ID, sendAction.getMessageDeduplicationId());
        }
        if (isNotBlank(sendAction.getMessageGroupId())) {
            result.put(SQS_MESSAGE_GROUP_ID, sendAction.getMessageGroupId());
        }
    }

    private void addSqsAdditionalMetaDataForReceiveAction(final ReceiveSqsMessage receiveAction,
                                                          final CommandResult result) {
        if (nonNull(receiveAction.getMaxNumberOfMessages())) {
            result.put(SQS_MAX_NUMBER_OF_MESSAGES, receiveAction.getMaxNumberOfMessages());
        }
        if (nonNull(receiveAction.getVisibilityTimeout())) {
            result.put(SQS_VISIBILITY_TIMEOUT, receiveAction.getVisibilityTimeout());
        }
        if (nonNull(receiveAction.getWaitTimeSeconds())) {
            result.put(SQS_WAIT_TIME_SECONDS, receiveAction.getWaitTimeSeconds());
        }
        if (isNotBlank(receiveAction.getReceiveRequestAttemptId())) {
            result.put(SQS_RECEIVE_REQUEST_ATTEMPT_ID, receiveAction.getReceiveRequestAttemptId());
        }
    }

    @SneakyThrows
    public void writeFullTestCycleExecutionResult(final TestExecutionSummary testExecutionSummary) {
        File executionResultFile = new File(TestResourceSettings.getInstance().getTestResourcesFolder(),
                EXECUTION_RESULT_FILENAME);
        String result = CollectionUtils.isNotEmpty(testExecutionSummary.getFailures())
                || testExecutionSummary.getTestsAbortedCount() > 0 ? FAILED : SUCCESSFULLY;
        FileUtils.write(executionResultFile, result, StandardCharsets.UTF_8);
    }

    public void addImageComparisonMetaData(final Image image, final CommandResult result) {
        result.put(IMAGE_FOR_COMPARISON, image.getFile());
        result.put(HIGHLIGHT_DIFFERENCE, image.isHighlightDifference());
        CompareWith compareWith = image.getCompareWith();
        if (nonNull(compareWith)) {
            result.put(IMAGE_COMPARISON_TYPE, EXTRACT_THEN_COMPARE);
            result.put(IMAGE_LOCATOR, compareWith.getLocatorId());
            result.put(IMAGE_SOURCE_ATT, compareWith.getAttribute());
        } else {
            result.put(IMAGE_COMPARISON_TYPE, TAKE_SCREENSHOT_THEN_COMPARE);
        }
    }

    public void addAssertAttributeMetaData(final Attribute attribute, final CommandResult result) {
        result.put(ASSERT_LOCATOR, attribute.getLocatorId());
        result.put(ASSERT_ATTRIBUTE, attribute.getName());
    }

    public void addSwipeMetaData(final SwipeNative swipeNative, final CommandResult result) {
        result.put(SWIPE_TYPE, swipeNative.getType().value());
        result.put(SWIPE_QUANTITY, swipeNative.getQuantity());
        result.put(PERFORM_SWIPE, swipeNative.getDirection());
        result.put(SWIPE_VALUE, swipeNative.getPercent());
        if (isNotBlank(swipeNative.getLocatorId())) {
            result.put(SWIPE_LOCATOR, swipeNative.getLocatorId());
        }
    }
}
