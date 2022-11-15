package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.websocket.WebsocketMessageHandler;
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
import com.knubisoft.cott.testing.model.scenario.WebSocket;
import com.knubisoft.cott.testing.model.scenario.WebSocketReceive;
import com.knubisoft.cott.testing.model.scenario.WebSocketSend;
import com.knubisoft.cott.testing.model.scenario.WebSocketTopic;
import lombok.SneakyThrows;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.cott.testing.framework.configuration.websocket.WebsocketConfiguration.WebsocketConnectionSupplier;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEBSOCKET_CONNECTION_FAILURE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEBSOCKET_NOT_ALL_MESSAGES_RECEIVED;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.UNABLE_TO_DISCONNECT_BECAUSE_CONNECTION_CLOSED;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(WebSocket.class)
public class WebsocketInterpreter extends AbstractInterpreter<WebSocket> {

    private final Map<String, StompSession> aliasToStompSession = new HashMap<>();
    private final Map<String, WebsocketMessageHandler> topicToMessageHandler = new HashMap<>();

    private StompSession stompSession;

    @Autowired(required = false)
    private Map<String, WebsocketConnectionSupplier> websocketConnections;

    public WebsocketInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final WebSocket websocket, final CommandResult commandResult) {
        List<CommandResult> subCommandsResultList = new LinkedList<>();
        commandResult.setSubCommandsResult(subCommandsResultList);
        processWebsockets(websocket, subCommandsResultList);
        ResultUtil.setExecutionResultIfSubCommandsFailed(commandResult);
    }

    private void processWebsockets(final WebSocket websocket,
                                   final List<CommandResult> subCommandsResultList) {
        executeConnection(websocket);
        subscribeToTopics(websocket);
        runActions(websocket, subCommandsResultList);
        disconnectFromSessionIfEnabled(websocket);
    }

    private void executeConnection(final WebSocket websocket) {
        String alias = websocket.getAlias();
        try {
            this.stompSession = aliasToStompSession.get(alias);
            if (Objects.isNull(stompSession) || !stompSession.isConnected()) {
                WebsocketConnectionSupplier websocketConnection = websocketConnections.get(alias);
                this.stompSession = websocketConnection.get().get();
                aliasToStompSession.put(alias, stompSession);
            }
        } catch (Exception e) {
            LogUtil.logException(e);
            throw new DefaultFrameworkException(format(WEBSOCKET_CONNECTION_FAILURE, alias), e);
        }
    }

    private void subscribeToTopics(final WebSocket websocket) {
        websocket.getTopic().stream()
                .distinct()
                .map(WebSocketTopic::getValue)
                .forEach(topic -> {
                    WebsocketMessageHandler handler = new WebsocketMessageHandler();
                    topicToMessageHandler.put(topic, handler);
                    stompSession.subscribe(topic, handler);
                });
    }

    private void runActions(final WebSocket websocket,
                            final List<CommandResult> subCommandsResultList) {
        websocket.getSendOrReceive().forEach(action -> {
            LogUtil.logSubCommand(dependencies.getPosition().incrementAndGet(), action);
            CommandResult result = ResultUtil.createNewCommandResultInstance(dependencies.getPosition().intValue());
            processEachAction(action, websocket.getAlias(), result);
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
                               final CommandResult result) {
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
        LogUtil.logWebsocketActionInfo(SEND_ACTION, wsSend.getEndpoint(), message);
        stompSession.send(wsSend.getEndpoint(), message);

        if (Objects.nonNull(wsSend.getReceive())) {
            receiveMessages(wsSend.getReceive(), alias, result);
            ResultUtil.addWebsocketInfoForSendAndReceiveAction(result);
        }
    }

    private void receiveMessages(final WebSocketReceive wsReceive,
                                 final String alias,
                                 final CommandResult result) {
        final String expectedContent = getExpectedContent(wsReceive);
        ResultUtil.addWebsocketInfoForReceiveAction(wsReceive, alias, result);
        LogUtil.logWebsocketActionInfo(RECEIVE_ACTION, wsReceive.getTopic(), expectedContent);

        final List<String> contentToCompare = getActualMessages(wsReceive);

        result.setActual(PrettifyStringJson.getJSONResult(toString(contentToCompare)));
        result.setExpected(PrettifyStringJson.getJSONResult(expectedContent));

        executeComparison(contentToCompare, expectedContent);
    }

    private List<String> getActualMessages(final WebSocketReceive wsReceive) {
        WebsocketMessageHandler handler = topicToMessageHandler.get(wsReceive.getTopic());
        LinkedList<String> receivedMessages = handler.getReceivedMessages();

        checkMessagesReceived(wsReceive, receivedMessages);

        return IntStream.range(0, wsReceive.getValuesNumber())
                .mapToObj(id -> receivedMessages.pollFirst())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private void checkMessagesReceived(final WebSocketReceive wsReceive,
                                       final LinkedList<String> receivedMessages) {
        int requiredMessagesNumber = wsReceive.getValuesNumber();
        if (requiredMessagesNumber > receivedMessages.size()) {
            TimeUnit.MILLISECONDS.sleep(wsReceive.getTimeoutMillis());
            if (requiredMessagesNumber > receivedMessages.size()) {
                log.error(format(
                        WEBSOCKET_NOT_ALL_MESSAGES_RECEIVED, requiredMessagesNumber - receivedMessages.size()));
            }
        }
    }

    private void executeComparison(final List<String> actualContent,
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

    private void disconnectFromSessionIfEnabled(final WebSocket websocket) {
        boolean isDisconnectEnabled = websocket.isDisconnect() && Objects.nonNull(stompSession);
        if (isDisconnectEnabled && stompSession.isConnected()) {
            stompSession.disconnect();
        } else if (isDisconnectEnabled && !stompSession.isConnected()) {
            log.error(UNABLE_TO_DISCONNECT_BECAUSE_CONNECTION_CLOSED);
        }
    }
}
