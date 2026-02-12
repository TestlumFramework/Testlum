package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.EMPTY;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(Websocket.class)
public class WebsocketInterpreter extends AbstractInterpreter<Websocket> {

    private static final int ALL_AVAILABLE_MESSAGES = 0;
    private static final int CHECK_PERIOD_MS = 100;

    //LOGS
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001b[36m";
    private static final String NEW_LOG_LINE = format("%n%19s| ", StringUtils.EMPTY);
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String EXCEPTION_LOG = ANSI_RED
            + "----------------    EXCEPTION    -----------------"
            + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
            + "--------------------------------------------------" + ANSI_RESET;
    private static final String COMMAND_LOG = ANSI_CYAN + "------- Command #{} - {} -------" + ANSI_RESET;
    private static final String WEBSOCKET_ACTION_INFO_LOG = format(TABLE_FORMAT,
            "Comment", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT,
            "Action", "{}");
    private static final String DESTINATION_LOG = format(TABLE_FORMAT, "Destination", "{}");
    private static final String CONTENT_LOG = format(TABLE_FORMAT, "Content", "{}");
    private static final String CONTENT_FORMAT = format("%n%19s| %-23s|", StringUtils.EMPTY, StringUtils.EMPTY);
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String SUBSCRIBE = "subscribe";
    private static final String SEND_ACTION = "send";
    private static final String RECEIVE_ACTION = "receive";

    //RESULT
    private static final String ALIAS = "Alias";
    private static final String MESSAGE_TO_SEND = "Message to send";
    private static final String STEP_FAILED = "Step failed";
    private static final String SEND = "Send";
    private static final String ENDPOINT = "Endpoint";
    private static final String RECEIVE = "Receive";
    private static final String TOPIC = "Topic";
    private static final String NUMBER_OF_MESSAGES = "Number of messages";
    private static final String TIMEOUT_MILLIS = "Timeout millis";
    private static final String ACTION = "Action";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    //EXCEPTIONS
    private static final String WEBSOCKET_CONNECTION_FAILURE =
            "Something went wrong while connecting to websocket with name <%s>";
    private static final String UNKNOWN_WEBSOCKET_COMMAND = "Unknown websocket command: %s";

    @Autowired(required = false)
    private Map<AliasEnv, WebsocketConnectionManager> wsConnectionSupplier;

    public WebsocketInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Websocket o, final CommandResult result) {
        Websocket websocket = injectCommand(o);
        checkAlias(websocket);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        processWebsockets(websocket, subCommandsResult);
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void checkAlias(final Websocket websocket) {
        if (websocket.getAlias() == null) {
            websocket.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void processWebsockets(final Websocket websocket, final List<CommandResult> subCommandsResult) {
        AliasEnv aliasEnv = new AliasEnv(websocket.getAlias(), dependencies.getEnvironment());
        logAlias(websocket.getAlias());
        openConnection(aliasEnv);
        runActions(websocket, subCommandsResult);
        disconnectIfEnabled(websocket.isDisconnect(), aliasEnv);
    }

    private void openConnection(final AliasEnv aliasEnv) {
        try {
            WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
            if (!wsConnectionManager.isConnected()) {
                wsConnectionManager.openConnection();
            }
        } catch (Exception e) {
            logException(e);
            throw new DefaultFrameworkException(format(WEBSOCKET_CONNECTION_FAILURE, aliasEnv.getAlias()), e);
        }
    }

    private void runActions(final Websocket websocket, final List<CommandResult> subCommandsResult) {
        List<Object> websocketActions = isNull(websocket.getStomp())
                ? websocket.getSendOrReceive()
                : websocket.getStomp().getSubscribeOrSendOrReceive();
        websocketActions.forEach(action -> {
            logSubCommand(dependencies.getPosition().incrementAndGet(), action);
            CommandResult commandResult = newCommandResultInstance(dependencies.getPosition().get());
            subCommandsResult.add(commandResult);
            processEachAction(action, websocket.getAlias(), commandResult);
        });
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            executeAction(action, alias, result);
        } catch (Exception e) {
            setExceptionResult(result, e);
            logException(e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getDuration().toMillis());
            stopWatch.stop();
        }
    }

    private void executeAction(final Object action,
                               final String alias,
                               final CommandResult result) throws IOException {
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof WebsocketSend) {
            sendMessage((WebsocketSend) action, aliasEnv, result);
        } else if (action instanceof WebsocketReceive) {
            receiveMessages((WebsocketReceive) action, aliasEnv, result);
        } else if (action instanceof WebsocketSubscribe) {
            subscribeToTopic((WebsocketSubscribe) action, aliasEnv, result);
        } else {
            throw new DefaultFrameworkException(UNKNOWN_WEBSOCKET_COMMAND, action.getClass().getSimpleName());
        }
    }

    private void sendMessage(final WebsocketSend wsSend,
                             final AliasEnv aliasEnv,
                             final CommandResult result) throws IOException {
        final String message = getMessageToSend(wsSend);
        addWebsocketInfoForSendAction(wsSend, aliasEnv.getAlias(), message, result);
        logWebsocketActionInfo(SEND_ACTION, wsSend.getComment(), wsSend.getEndpoint(), message);

        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        wsConnectionManager.sendMessage(wsSend, message);
    }

    private void receiveMessages(final WebsocketReceive wsReceive,
                                 final AliasEnv aliasEnv,
                                 final CommandResult result) {
        final String expectedContent = getExpectedContent(wsReceive);
        addWebsocketInfoForReceiveAction(wsReceive, aliasEnv.getAlias(), result);
        logWebsocketActionInfo(RECEIVE_ACTION, wsReceive.getComment(), wsReceive.getTopic(), expectedContent);

        final List<Object> actualContent = getMessagesToCompare(wsReceive, aliasEnv);
        result.setActual(StringPrettifier.asJsonResult(toString(actualContent)));
        result.setExpected(StringPrettifier.asJsonResult(expectedContent));

        executeComparison(actualContent, expectedContent);
    }

    private List<Object> getMessagesToCompare(final WebsocketReceive wsReceive, final AliasEnv aliasEnv) {
        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        LinkedList<String> receivedMessages = wsConnectionManager.receiveMessages(wsReceive);
        return achieveRequiredMessageCount(wsReceive, receivedMessages);
    }

    private List<Object> achieveRequiredMessageCount(final WebsocketReceive wsReceive,
                                                     final LinkedList<String> receivedMessages) {
        int requiredMessageCount = nonNull(wsReceive.getLimit())
                ? wsReceive.getLimit().intValue() : ALL_AVAILABLE_MESSAGES;

        checkMessagesAreReceived(requiredMessageCount, wsReceive.getTimeoutMillis(), receivedMessages);

        int limit = (requiredMessageCount <= ALL_AVAILABLE_MESSAGES) ? receivedMessages.size() : requiredMessageCount;
        return IntStream.range(0, limit)
                .mapToObj(id -> receivedMessages.pollFirst())
                .filter(Objects::nonNull)
                .map(JacksonMapperUtil::toJsonObject)
                .collect(Collectors.toList());
    }

    private void checkMessagesAreReceived(final int requiredMessageCount,
                                          final long timeoutMillis,
                                          final LinkedList<String> receivedMessages) {
        if (requiredMessageCount <= ALL_AVAILABLE_MESSAGES && timeoutMillis > 0) {
            sleep(timeoutMillis);
        }
        if (requiredMessageCount > receivedMessages.size() && timeoutMillis > 0) {
            waitUntil(() -> receivedMessages.size() >= requiredMessageCount,
                    timeoutMillis);
        }
    }

    private void executeComparison(final List<Object> actualContent, final String expectedContent) {
        CompareBuilder comparator = newCompare()
                .withExpected(expectedContent)
                .withActual(actualContent);
        comparator.exec();
    }

    private void subscribeToTopic(final WebsocketSubscribe wsSubscribe,
                                  final AliasEnv aliasEnv,
                                  final CommandResult result) {
        addWebsocketInfoForSubscribeAction(wsSubscribe, aliasEnv.getAlias(), result);
        logWebsocketActionInfo(SUBSCRIBE, wsSubscribe.getComment(), wsSubscribe.getTopic(), EMPTY);
        wsConnectionSupplier.get(aliasEnv).subscribeTo(wsSubscribe.getTopic());
    }

    private String getMessageToSend(final WebsocketSend wsSend) {
        return getValue(wsSend.getMessage(), wsSend.getFile());
    }

    private String getExpectedContent(final WebsocketReceive wsReceive) {
        return getValue(wsReceive.getMessage(), wsReceive.getFile());
    }

    private String getValue(final String message, final String file) {
        return isNotBlank(message)
                ? message
                : getContentIfFile(file);
    }

    @SneakyThrows
    private void disconnectIfEnabled(final boolean isDisconnectEnabled, final AliasEnv aliasEnv) {
        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        if (isDisconnectEnabled) {
            wsConnectionManager.closeConnection();
        }
    }

    private void sleep(final long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException ignored) {
            //ignored
        }
    }

    private void waitUntil(final BooleanSupplier condition, final long timeoutMillis) {
        long start = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - start > timeoutMillis) {
                return;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(CHECK_PERIOD_MS);
            } catch (InterruptedException ignored) {
                //ignored
            }
        }
    }

    //LOGS
    private void logAlias(final String alias) {
        log.info(ALIAS_LOG, alias);
    }

    private void logException(final Exception ex) {
        if (isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    private void logSubCommand(final int position, final Object action) {
        log.info(COMMAND_LOG, position, action.getClass().getSimpleName());
    }

    private void logWebsocketActionInfo(final String action,
                                        final String comment,
                                        final String destination,
                                        final String content) {
        log.info(WEBSOCKET_ACTION_INFO_LOG, comment, action.toUpperCase(Locale.ROOT));
        if (isNotBlank(destination)) {
            log.info(DESTINATION_LOG, destination);
        }
        if (isNotBlank(content)) {
            log.info(CONTENT_LOG, StringPrettifier.asJsonResult(content).replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    //RESULT
    private void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult
                    .stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    private void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    private void addWebsocketInfoForSendAction(final WebsocketSend sendAction,
                                               final String alias,
                                               final String message,
                                               final CommandResult result) {
        addWebsocketGeneralInfo(SEND, sendAction.getComment(), alias, ENDPOINT, sendAction.getEndpoint(), result);
        result.put(MESSAGE_TO_SEND, message);
    }

    private void addWebsocketInfoForReceiveAction(final WebsocketReceive receiveAction,
                                                  final String alias,
                                                  final CommandResult result) {
        addWebsocketGeneralInfo(RECEIVE, receiveAction.getComment(), alias, TOPIC, receiveAction.getTopic(), result);
        result.put(NUMBER_OF_MESSAGES, nonNull(receiveAction.getLimit())
                ? receiveAction.getLimit().intValue() : ALL_AVAILABLE_MESSAGES);
        result.put(TIMEOUT_MILLIS, receiveAction.getTimeoutMillis());
    }

    private void addWebsocketInfoForSubscribeAction(final WebsocketSubscribe subscribe,
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

    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }
}
