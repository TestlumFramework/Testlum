package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractMessageBrokerInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Websocket;
import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSubscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;


@Slf4j
@InterpreterForClass(Websocket.class)
public class WebsocketInterpreter extends AbstractMessageBrokerInterpreter<Websocket> {

    private static final int ALL_AVAILABLE_MESSAGES = 0;
    private static final int CHECK_PERIOD_MS = 100;

    private static final String WEBSOCKET_ACTION_INFO_LOG =
            LogFormat.table("Comment") + LogFormat.newLogLine() + LogFormat.table("Action");
    private static final String DESTINATION_LOG = LogFormat.table("Destination");
    private static final String CONTENT_LOG = LogFormat.table("Content");
    private static final String SUBSCRIBE = "subscribe";
    private static final String ENDPOINT = "Endpoint";
    private static final String TOPIC = "Topic";
    private static final String NUMBER_OF_MESSAGES = "Number of messages";
    private static final String WEBSOCKET_CONNECTION_FAILURE =
            "Something went wrong while connecting to websocket with name <%s>";
    private static final String UNKNOWN_WEBSOCKET_COMMAND = "Unknown websocket command: %s";
    private static final String WS_NOT_CONFIGURED = "WebSocket integration is not configured for this environment";

    @Autowired(required = false)
    private Map<AliasEnv, WebsocketConnectionManager> wsConnectionSupplier;

    public WebsocketInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected String getAlias(final Websocket command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Websocket command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<Object> getActions(final Websocket command) {
        return Objects.isNull(command.getStomp())
                ? command.getSendOrReceive()
                : command.getStomp().getSubscribeOrSendOrReceive();
    }

    @Override
    protected void processAction(final Object action, final String alias, final CommandResult result) {
        try {
            executeAction(action, alias, result);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    @Override
    protected void validate() {
        if (wsConnectionSupplier == null) {
            throw new DefaultFrameworkException(WS_NOT_CONFIGURED);
        }
    }

    @Override
    protected void beforeActions(final Websocket command, final CommandResult result) {
        logAlias(command.getAlias());
        openConnection(toAliasEnv(command));
    }

    @Override
    protected void afterActions(final Websocket command, final CommandResult result) {
        disconnectIfEnabled(command.isDisconnect(), toAliasEnv(command));
    }

    private AliasEnv toAliasEnv(final Websocket command) {
        return new AliasEnv(command.getAlias(), dependencies.getEnvironment());
    }

    private void openConnection(final AliasEnv aliasEnv) {
        try {
            WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
            if (!wsConnectionManager.isConnected()) {
                wsConnectionManager.openConnection();
            }
        } catch (Exception e) {
            logException(e);
            throw new DefaultFrameworkException(String.format(WEBSOCKET_CONNECTION_FAILURE, aliasEnv.getAlias()), e);
        }
    }

    private void executeAction(final Object action,
                               final String alias,
                               final CommandResult result) throws IOException {
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof WebsocketSend wsSend) {
            sendMessage(wsSend, aliasEnv, result);
        } else if (action instanceof WebsocketReceive wsReceive) {
            receiveMessages(wsReceive, aliasEnv, result);
        } else if (action instanceof WebsocketSubscribe wsSubscribe) {
            subscribeToTopic(wsSubscribe, aliasEnv, result);
        } else {
            throw new DefaultFrameworkException(UNKNOWN_WEBSOCKET_COMMAND, action.getClass().getSimpleName());
        }
    }

    private void sendMessage(final WebsocketSend wsSend,
                             final AliasEnv aliasEnv,
                             final CommandResult result) throws IOException {
        final String message = getValue(wsSend.getMessage(), wsSend.getFile());
        addWebsocketInfoForSendAction(wsSend, aliasEnv.getAlias(), message, result);
        logWebsocketActionInfo(SEND_ACTION, wsSend.getComment(), wsSend.getEndpoint(), message);

        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        wsConnectionManager.sendMessage(wsSend, message);
    }

    private void receiveMessages(final WebsocketReceive wsReceive,
                                 final AliasEnv aliasEnv,
                                 final CommandResult result) {
        final String expectedContent = getValue(wsReceive.getMessage(), wsReceive.getFile());
        addWebsocketInfoForReceiveAction(wsReceive, aliasEnv.getAlias(), result);
        logWebsocketActionInfo(RECEIVE_ACTION, wsReceive.getComment(), wsReceive.getTopic(), expectedContent);

        final List<Object> actualContent = getMessagesToCompare(wsReceive, aliasEnv);
        result.setActual(stringPrettifier.asJsonResult(toString(actualContent)));
        result.setExpected(stringPrettifier.asJsonResult(expectedContent));

        executeComparison(actualContent, expectedContent);
    }

    private List<Object> getMessagesToCompare(final WebsocketReceive wsReceive, final AliasEnv aliasEnv) {
        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        Deque<String> receivedMessages = wsConnectionManager.receiveMessages(wsReceive);
        return achieveRequiredMessageCount(wsReceive, receivedMessages);
    }

    private List<Object> achieveRequiredMessageCount(final WebsocketReceive wsReceive,
                                                     final Deque<String> receivedMessages) {
        int requiredMessageCount = Objects.nonNull(wsReceive.getLimit())
                ? wsReceive.getLimit().intValue() : ALL_AVAILABLE_MESSAGES;

        checkMessagesAreReceived(requiredMessageCount, wsReceive.getTimeoutMillis(), receivedMessages);

        int limit = (requiredMessageCount <= ALL_AVAILABLE_MESSAGES) ? receivedMessages.size() : requiredMessageCount;
        return IntStream.range(0, limit)
                .mapToObj(id -> receivedMessages.pollFirst())
                .filter(Objects::nonNull)
                .map(jacksonService::toJsonObject)
                .toList();
    }

    private void checkMessagesAreReceived(final int requiredMessageCount,
                                          final long timeoutMillis,
                                          final Deque<String> receivedMessages) {
        if (requiredMessageCount <= ALL_AVAILABLE_MESSAGES && timeoutMillis > 0) {
            sleep(timeoutMillis);
        }
        if (requiredMessageCount > receivedMessages.size() && timeoutMillis > 0) {
            waitUntil(() -> receivedMessages.size() >= requiredMessageCount, timeoutMillis);
        }
    }

    private void executeComparison(final List<Object> actualContent, final String expectedContent) {
        newCompare()
                .withExpected(expectedContent)
                .withActual(actualContent)
                .exec();
    }

    private void subscribeToTopic(final WebsocketSubscribe wsSubscribe,
                                  final AliasEnv aliasEnv,
                                  final CommandResult result) {
        addWebsocketInfoForSubscribeAction(wsSubscribe, aliasEnv.getAlias(), result);
        logWebsocketActionInfo(SUBSCRIBE, wsSubscribe.getComment(), wsSubscribe.getTopic(), StringUtils.EMPTY);
        wsConnectionSupplier.get(aliasEnv).subscribeTo(wsSubscribe.getTopic());
    }

    private void disconnectIfEnabled(final boolean isDisconnectEnabled, final AliasEnv aliasEnv) {
        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        if (isDisconnectEnabled) {
            try {
                wsConnectionManager.closeConnection();
            } catch (Exception e) {
                throw new DefaultFrameworkException(e);
            }
        }
    }

    private void sleep(final long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void logAlias(final String alias) {
        log.info(ALIAS_LOG, alias);
    }

    private void logWebsocketActionInfo(final String action,
                                        final String comment,
                                        final String destination,
                                        final String content) {
        log.info(WEBSOCKET_ACTION_INFO_LOG, comment, action.toUpperCase(Locale.ROOT));
        if (StringUtils.isNotBlank(destination)) {
            log.info(DESTINATION_LOG, destination);
        }
        if (StringUtils.isNotBlank(content)) {
            log.info(CONTENT_LOG, stringPrettifier.asJsonResult(content).
                    replaceAll(LogFormat.newLine(), LogFormat.contentFormat()));
        }
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
        result.put(NUMBER_OF_MESSAGES, Objects.nonNull(receiveAction.getLimit())
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
        if (StringUtils.isNotBlank(destinationValue)) {
            result.put(destination, destinationValue);
        }
    }
}
