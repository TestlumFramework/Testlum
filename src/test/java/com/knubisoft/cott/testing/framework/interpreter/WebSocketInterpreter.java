package com.knubisoft.cott.testing.framework.interpreter;

import com.google.common.collect.ImmutableList;
import com.knubisoft.cott.testing.framework.configuration.websocket.WebSocketMessageHandler;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.ScenarioUtil;
import com.knubisoft.cott.testing.model.scenario.CompareRule;
import com.knubisoft.cott.testing.model.scenario.WebSocket;
import com.knubisoft.cott.testing.model.scenario.WebSocketReceive;
import com.knubisoft.cott.testing.model.scenario.WebSocketSend;
import com.knubisoft.cott.testing.model.scenario.WebSocketTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.knubisoft.cott.testing.framework.configuration.websocket.WebSocketConfiguration.WebSocketConnectionSupplier;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEBSOCKET_CONNECTION_FAILURE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEBSOCKET_NOT_ALL_MESSAGES_RECEIVED;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.UNABLE_TO_DISCONNECT_BECAUSE_CONNECTION_CLOSED;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(WebSocket.class)
public class WebSocketInterpreter extends AbstractInterpreter<WebSocket> {

    private final Map<String, StompSession> aliasToStompSession = new HashMap<>();
    private final Map<String, WebSocketMessageHandler> topicToMessageHandler = new HashMap<>();

    private StompSession stompSession;

    @Autowired(required = false)
    private Map<String, WebSocketConnectionSupplier> webSocketConnectionSupplier;

    public WebSocketInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final WebSocket webSocket, final CommandResult commandResult) {
        List<CommandResult> subCommandsResultList = new LinkedList<>();
        commandResult.setSubCommandsResult(subCommandsResultList);
        processWebSockets(webSocket, subCommandsResultList);
        ResultUtil.setExecutionResultIfSubCommandsFailed(commandResult);
    }

    private void processWebSockets(final WebSocket webSocket,
                                   final List<CommandResult> subCommandsResultList) {
        executeConnection(webSocket);
        subscribeToTopics(webSocket);
        runActions(webSocket, subCommandsResultList);
        disconnectFromSessionIfEnabled(webSocket);
    }

    private void executeConnection(final WebSocket webSocket) {
        String alias = webSocket.getAlias();
        try {
            this.stompSession = aliasToStompSession.get(alias);
            if (Objects.isNull(stompSession) || !stompSession.isConnected()) {
                WebSocketConnectionSupplier webSocketConnection = webSocketConnectionSupplier.get(alias);
                this.stompSession = webSocketConnection.get().get();
                aliasToStompSession.put(alias, stompSession);
            }
        } catch (Exception e) {
            LogUtil.logException(e);
            throw new DefaultFrameworkException(format(WEBSOCKET_CONNECTION_FAILURE, alias), e);
        }
    }

    private void subscribeToTopics(final WebSocket webSocket) {
        webSocket.getTopic().stream()
                .distinct()
                .map(WebSocketTopic::getValue)
                .forEach(topic -> {
                    WebSocketMessageHandler handler = new WebSocketMessageHandler();
                    topicToMessageHandler.put(topic, handler);
                    stompSession.subscribe(topic, handler);
                });
    }

    private void runActions(final WebSocket webSocket,
                            final List<CommandResult> subCommandsResultList) {
        webSocket.getSendOrReceive().forEach(action -> {
            LogUtil.logSubCommand(dependencies.getPosition().incrementAndGet(), action);
            CommandResult result = ResultUtil.createNewCommandResultInstance(dependencies.getPosition().intValue());
            processEachAction(action, webSocket.getAlias(), result);
            subCommandsResultList.add(result);
        });
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult result) {
        LogUtil.logAlias(alias);
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            executeAction(action, alias, result);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(result, e);
            LogUtil.logException(e);
            ScenarioUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void executeAction(final Object action,
                               final String alias,
                               final CommandResult result) throws InterruptedException {
        if (action instanceof WebSocketSend) {
            sendMessage((WebSocketSend) action, alias, result);
        } else {
            receiveMessages((WebSocketReceive) action, alias, result);
        }
    }

    private void sendMessage(final WebSocketSend wsSend,
                             final String alias,
                             final CommandResult result) {
        final String message = getMessageToSend(wsSend);
        ResultUtil.addWebsocketInfoForSendAction(wsSend, alias, message, result);
        LogUtil.logWebSocketActionInfo(SEND_ACTION, wsSend.getEndpoint(), message);
        stompSession.send(wsSend.getEndpoint(), message);
    }

    private void receiveMessages(final WebSocketReceive wsReceive,
                                 final String alias,
                                 final CommandResult result) throws InterruptedException {
        final String expectedContent = getExpectedContent(wsReceive);
        ResultUtil.addWebsocketInfoForReceiveAction(wsReceive, alias, result);
        LogUtil.logWebSocketActionInfo(RECEIVE_ACTION, wsReceive.getTopic(), expectedContent);

        List<String> receivedMessages = getActualMessages(wsReceive);
        String contentToCompare = getMessagesToCompareByRule(wsReceive, receivedMessages, expectedContent);

        result.setActual(PrettifyStringJson.getJSONResult(contentToCompare));
        result.setExpected(PrettifyStringJson.getJSONResult(expectedContent));

        executeComparison(contentToCompare, expectedContent);
    }

    private List<String> getActualMessages(final WebSocketReceive wsReceive) throws InterruptedException {
        WebSocketMessageHandler handler = topicToMessageHandler.get(wsReceive.getTopic());
        int minimumMessagesNumber = wsReceive.getValuesNumber().intValue();
        List<String> receivedMessages = handler.getReceivedMessages();

        if (minimumMessagesNumber > receivedMessages.size()) {
            TimeUnit.MILLISECONDS.sleep(wsReceive.getTimeoutMillis());
            if (minimumMessagesNumber > receivedMessages.size()) {
                throw new DefaultFrameworkException(WEBSOCKET_NOT_ALL_MESSAGES_RECEIVED,
                        minimumMessagesNumber - receivedMessages.size());
            }
        }
        return ImmutableList.copyOf(receivedMessages);
    }

    private String getMessagesToCompareByRule(final WebSocketReceive wsReceive,
                                              final List<String> receivedMessages,
                                              final String expectedContent) {
        CompareRule compareRule = wsReceive.getCompareRule();

        if (CompareRule.EQUALS == compareRule) {
            return toString(receivedMessages.subList(0, wsReceive.getValuesNumber().intValue()));
        }
        if (CompareRule.CONTAINS == compareRule) {
            return receivedMessages.stream()
                    .filter(message -> Objects.equals(message, expectedContent))
                    .findFirst()
                    .orElseGet(() -> toString(receivedMessages));
        }
        return toString(receivedMessages);
    }

    private void executeComparison(final String actualContent,
                                   final String expectedContent) {
        CompareBuilder comparator = newCompare()
                .withExpected(expectedContent)
                .withActual(actualContent);
        comparator.exec();
    }

    private String getMessageToSend(final WebSocketSend wsSend) {
        return getValue(wsSend.getMessage(), wsSend.getFile());
    }

    private String getExpectedContent(final WebSocketReceive wsReceive) {
        return getValue(wsReceive.getMessage(), wsReceive.getFile());
    }

    private String getValue(final String message, final String file) {
        return Objects.nonNull(message)
                ? message
                : FileSearcher.searchFileToString(file, dependencies.getFile());
    }

    private void disconnectFromSessionIfEnabled(final WebSocket webSocket) {
        boolean isDisconnectEnabled = webSocket.isDisconnect() && Objects.nonNull(stompSession);
        if (isDisconnectEnabled && stompSession.isConnected()) {
            stompSession.disconnect();
        } else if (isDisconnectEnabled && !stompSession.isConnected()) {
            log.error(UNABLE_TO_DISCONNECT_BECAUSE_CONNECTION_CLOSED);
        }
    }
}
