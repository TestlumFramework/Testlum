package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.ElasticSearchRequest;
import com.knubisoft.cott.testing.model.scenario.Header;
import com.knubisoft.cott.testing.model.scenario.Hovers;
import com.knubisoft.cott.testing.model.scenario.HttpInfo;
import com.knubisoft.cott.testing.model.scenario.KafkaHeaders;
import com.knubisoft.cott.testing.model.scenario.ReceiveKafkaMessage;
import com.knubisoft.cott.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.cott.testing.model.scenario.RmqHeaders;
import com.knubisoft.cott.testing.model.scenario.SendKafkaMessage;
import com.knubisoft.cott.testing.model.scenario.SendRmqMessage;
import com.knubisoft.cott.testing.model.scenario.SendgridInfo;
import com.knubisoft.cott.testing.model.scenario.Ses;
import com.knubisoft.cott.testing.model.scenario.SesBody;
import com.knubisoft.cott.testing.model.scenario.SesMessage;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.format;

@UtilityClass
public class ResultUtil {

    public static final String ALIAS = "Alias";
    public static final String API_ALIAS = "API alias";
    public static final String AUTHENTICATION_TYPE = "Authentication type";
    public static final String CREDENTIALS_FILE = "Credentials file";
    public static final String POSTGRES = "POSTGRES";
    public static final String QUEUE = "Queue";
    public static final String TIME = "Time";
    public static final String MESSAGE_TO_SEND = "Message to send";
    public static final String CONTENT_TO_SEND = "Content to send";
    public static final String EXPECTED_CODE = "Expected code";
    public static final String ACTUAL_CODE = "Actual code";
    public static final String JSON_PATH = "JSON path";
    public static final String XPATH = "Xpath";
    public static final String POSTGRES_QUERY = "Postgres query";
    public static final String EXPRESSION = "Expression";
    public static final String NO_EXPRESSION = "No expression";
    public static final String CONSTANT = "Constant";
    public static final String ASSERT_LOCATOR = "Locator for assert command";
    public static final String ASSERT_ATTRIBUTE = "Assert command attribute";
    public static final String CLICK_LOCATOR = "Locator for click command";
    public static final String INPUT_LOCATOR = "Locator for input command";
    public static final String CLEAR_LOCATOR = "Locator for clear command";
    public static final String SCROLL_LOCATOR = "Locator for scroll-to command";
    public static final String INPUT_VALUE = "Value for input";
    public static final String CLICK_METHOD = "Click method";
    public static final String CLOSE_COMMAND = "Close command for";
    public static final String SECOND_TAB = "second tab";
    public static final String JS_FILE = "JS file to execute";
    public static final String NAVIGATE_TYPE = "Navigate command type";
    public static final String NAVIGATE_URL = "URL for navigate";
    public static final String DROP_DOWN_LOCATOR = "Locator for drop down command";
    public static final String DROP_DOWN_FOR = "Drop down command for";
    public static final String DROP_DOWN_BY = "Process by";
    public static final String ALL_VALUES_DESELECT = "all values (deselect)";
    public static final String ONE_VALUE_TEMPLATE = "one value (%s)";
    public static final String NUMBER_OF_REPETITIONS = "Number of repetitions";
    public static final String CLEAR_COOKIES_AFTER_EXECUTION = "Clear cookies after execution";
    public static final String CLEAR_LOCAL_STORAGE_BY_KEY = "Clear local storage by key";
    private static final String SCROLL_DIRECTION = "Scroll direction";
    private static final String SCROLL_MEASURE = "Scroll measure";
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
    private static final String HEADERS_STATUS = "Headers status";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String TOPIC = "Topic";
    private static final String ROUTING_KEY = "Routing Key";
    private static final String EXCHANGE = "Exchange";
    private static final String ACTION = "Action";
    private static final String SEND = "Send";
    private static final String COMMENT_FOR_KAFKA_SEND_ACTION = "Send message to Kafka";
    private static final String COMMENT_FOR_RABBIT_SEND_ACTION = "Send message to RabbitMQ";
    private static final String COMMENT_FOR_KAFKA_RECEIVE_ACTION = "Receive message from Kafka";
    private static final String COMMENT_FOR_RABBIT_RECEIVE_ACTION = "Receive message from RabbitMQ";
    private static final String TIMEOUT_MILLIS = "Timeout millis";
    private static final String KEY = "Key";
    private static final String BUCKET = "Bucket";
    private static final String CORRELATION_ID = "Correlation ID";
    private static final String RECEIVE = "Receive";
    private static final String DATABASE = "Database";
    private static final String DATABASE_ALIAS = "Database alias";
    private static final String PATCHES = "Patches";
    private static final String SHELL_FILES = "Shell files";
    private static final String SHELL_COMMANDS = "Shell commands";
    private static final String TYPE = "Type";
    private static final String VALUE = "Value";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String MOVE_TO_EMPTY_SPACE = "Move to empty space after execution";
    private static final String HOVER_NUMBER_TEMPLATE = "Hover #%d";
    private static final String COMMANDS_FOR_REPEAT = "Commands for repeat";
    private static final String STEP_FAILED = "Step failed";
    private static final String FAILED = "failed";
    private static final String SUCCESSFULLY = "successfully";
    private static final String EXECUTION_RESULT_FILENAME = "scenarios_execution_result.txt";

    public CommandResult createCommandResultForUiSubCommand(final int number, final String name, final String comment) {
        CommandResult subCommandResult = createNewCommandResultInstance(number);
        subCommandResult.setCommandKey(name);
        subCommandResult.setComment(comment);
        return subCommandResult;
    }

    public CommandResult createNewCommandResultInstance(final int number) {
        CommandResult subCommandResult = new CommandResult();
        subCommandResult.setId(number);
        subCommandResult.setSuccess(true);
        return subCommandResult;
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
            result.setSuccess(false);
            result.setException(exception);
        }
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
                                                final String destinationName,
                                                final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        result.put(destination, destinationName);
    }

    public void addHttpMetaData(final String alias,
                                final HttpInfo httpInfo,
                                final String httpMethodName,
                                final CommandResult commandResult) {
        commandResult.put(API_ALIAS, alias);
        commandResult.put(ENDPOINT, httpInfo.getUrl());
        commandResult.put(HTTP_METHOD, httpMethodName);
        List<Header> headers = httpInfo.getHeader();
        if (!headers.isEmpty()) {
            addHeaders(headers, commandResult);
        }
    }

    public static void addElasticsearchMetaData(final String alias,
                                                final ElasticSearchRequest elasticSearchRequest,
                                                final String httpMethodName,
                                                final CommandResult commandResult) {
        commandResult.put(ALIAS, alias);
        commandResult.put(ENDPOINT, elasticSearchRequest.getUrl());
        commandResult.put(HTTP_METHOD, httpMethodName);
        List<Header> headers = elasticSearchRequest.getHeader();
        if (!headers.isEmpty()) {
            addHeaders(headers, commandResult);
        }
    }


    public void addSendGridMetaData(final String alias,
                                    final SendgridInfo sendgridInfo,
                                    final String httpMethodName,
                                    final CommandResult commandResult) {
        commandResult.put(ALIAS, alias);
        commandResult.put(ENDPOINT, sendgridInfo.getUrl());
        commandResult.put(HTTP_METHOD, httpMethodName);
        List<Header> headers = sendgridInfo.getHeader();
        if (!headers.isEmpty()) {
            addHeaders(headers, commandResult);
        }
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
                                 final CommandResult commandResult) {
        if (!shellCommands.isEmpty()) {
            commandResult.put(SHELL_COMMANDS, shellCommands);
        }
        if (!shellFiles.isEmpty()) {
            commandResult.put(SHELL_FILES, shellFiles);
        }
    }

    public void addVariableMetaData(final String type,
                                    final String key,
                                    final String expression,
                                    final String value,
                                    final CommandResult commandResult) {
        commandResult.put(TYPE, type);
        commandResult.put(KEY, key);
        commandResult.put(EXPRESSION, expression);
        commandResult.put(VALUE, value);
    }

    public void addDropDownForOneValueMetaData(final String type,
                                               final String processBy,
                                               final String value,
                                               final CommandResult commandResult) {
        commandResult.put(DROP_DOWN_FOR, format(ONE_VALUE_TEMPLATE, type));
        commandResult.put(DROP_DOWN_BY, processBy);
        commandResult.put(VALUE, value);
    }

    public void addScrollMetaData(final String direction,
                                  final String measure,
                                  final String value,
                                  final CommandResult commandResult) {
        commandResult.put(SCROLL_DIRECTION, direction);
        commandResult.put(SCROLL_MEASURE, measure);
        commandResult.put(VALUE, value);
    }

    public void addHoversMetaData(final Hovers hovers, final CommandResult result) {
        Boolean isMoveToEmptySpace = hovers.isMoveToEmptySpace();
        if (isMoveToEmptySpace != null) {
            result.put(MOVE_TO_EMPTY_SPACE, isMoveToEmptySpace);
        }
        AtomicInteger number = new AtomicInteger(1);
        hovers.getHover().forEach(hover -> {
            String hoverNumber = format(HOVER_NUMBER_TEMPLATE, number.getAndIncrement());
            result.put(hoverNumber, Arrays.asList(hover.getLocatorId(), hovers.getComment()));
        });
    }

    public void addCommandsForRepeat(final List<AbstractCommand> commandsForRepeat, final CommandResult result) {
        result.put(COMMANDS_FOR_REPEAT, commandsForRepeat.stream()
                .map(command -> command.getClass().getSimpleName()).collect(Collectors.toList()));
    }

    private void addKafkaAdditionalMetaDataForSendAction(final SendKafkaMessage sendAction,
                                                         final CommandResult result) {
        String key = sendAction.getKey();
        String correlationId = sendAction.getCorrelationId();
        KafkaHeaders kafkaHeaders = sendAction.getHeaders();
        if (StringUtils.isNotEmpty(key)) {
            result.put(KEY, key);
        }
        if (StringUtils.isNotEmpty(correlationId)) {
            result.put(CORRELATION_ID, correlationId);
        }
        if (kafkaHeaders != null) {
            result.put(ADDITIONAL_HEADERS, kafkaHeaders.getHeader().stream().map(header ->
                    format(HEADER_TEMPLATE, header.getName(), header.getValue())).collect(Collectors.toList()));
        }
    }

    private void addRabbitMQAdditionalMetaDataForSendAction(final SendRmqMessage sendAction,
                                                            final CommandResult result) {
        String exchange = sendAction.getExchange();
        String correlationId = sendAction.getCorrelationId();
        RmqHeaders rabbitHeaders = sendAction.getHeaders();
        if (StringUtils.isNotEmpty(exchange)) {
            result.put(EXCHANGE, exchange);
        }
        if (StringUtils.isNotEmpty(correlationId)) {
            result.put(CORRELATION_ID, correlationId);
        }
        if (rabbitHeaders != null) {
            result.put(ADDITIONAL_HEADERS, rabbitHeaders.getHeader().stream().map(header ->
                    format(HEADER_TEMPLATE, header.getName(), header.getValue())).collect(Collectors.toList()));
        }
    }

    private void addHeaders(final List<Header> headers, final CommandResult commandResult) {
        commandResult.put(ADDITIONAL_HEADERS, headers.stream()
                .map(header ->
                        format(HEADER_TEMPLATE, header.getName(), header.getData())).collect(Collectors.toList()));
    }

    @SneakyThrows
    public static void writeFullTestCycleExecutionResult(final TestExecutionSummary testExecutionSummary) {
        File executionResultFile = new File(TestResourceSettings.getInstance().getTestResourcesFolder(),
                EXECUTION_RESULT_FILENAME);
        String result = CollectionUtils.isNotEmpty(testExecutionSummary.getFailures())
                || testExecutionSummary.getTestsAbortedCount() > 0 ? FAILED : SUCCESSFULLY;
        FileUtils.write(executionResultFile, result, StandardCharsets.UTF_8);
    }
}
