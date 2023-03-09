package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.websocket.WebsocketConnectionManager;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.ScenarioUtil;
import com.knubisoft.cott.testing.model.AliasEnv;
import com.knubisoft.cott.testing.model.scenario.Websocket;
import com.knubisoft.cott.testing.model.scenario.WebsocketReceive;
import com.knubisoft.cott.testing.model.scenario.WebsocketSend;
import com.knubisoft.cott.testing.model.scenario.WebsocketSubscribe;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.CLOSE_BRACE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.CLOSE_SQUARE_BRACKET;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEBSOCKET_CONNECTION_FAILURE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SUBSCRIBE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.UNABLE_TO_DISCONNECT_BECAUSE_CONNECTION_CLOSED;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Websocket.class)
public class WebsocketInterpreter extends AbstractInterpreter<Websocket> {

    @Autowired(required = false)
    private Map<AliasEnv, WebsocketConnectionManager> wsConnectionSupplier;

    public WebsocketInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Websocket websocket, final CommandResult commandResult) {
        List<CommandResult> subCommandsResultList = new LinkedList<>();
        commandResult.setSubCommandsResult(subCommandsResultList);
        processWebsockets(websocket, subCommandsResultList);
        ResultUtil.setExecutionResultIfSubCommandsFailed(commandResult);
    }

    private void processWebsockets(final Websocket websocket,
                                   final List<CommandResult> subCommandsResultList) {
        AliasEnv aliasEnv = new AliasEnv(websocket.getAlias(), dependencies.getEnvironment());
        openConnection(aliasEnv);
        runActions(websocket, subCommandsResultList);
        disconnectIfEnabled(websocket.isDisconnect(), aliasEnv);
    }

    private void openConnection(final AliasEnv aliasEnv) {
        LogUtil.logAlias(aliasEnv.getAlias());
        try {
            WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
            if (!wsConnectionManager.isConnected()) {
                wsConnectionManager.openConnection();
            }
        } catch (Exception e) {
            LogUtil.logException(e);
            throw new DefaultFrameworkException(format(WEBSOCKET_CONNECTION_FAILURE, aliasEnv.getAlias()), e);
        }
    }

    private void runActions(final Websocket websocket,
                            final List<CommandResult> subCommandsResultList) {
        List<Object> websocketActions = Objects.isNull(websocket.getStomp())
                ? websocket.getSendOrReceive()
                : websocket.getStomp().getSubscribeOrSendOrReceive();
        websocketActions.forEach(action -> {
            LogUtil.logSubCommand(dependencies.getPosition().incrementAndGet(), action);
            CommandResult result = ResultUtil.createNewCommandResultInstance(dependencies.getPosition().intValue());
            processEachAction(action, websocket.getAlias(), result);
            subCommandsResultList.add(result);
        });
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult result) {
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
                               final CommandResult result) throws IOException {
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof WebsocketSend) {
            sendMessage((WebsocketSend) action, aliasEnv, result);
        } else if (action instanceof WebsocketReceive) {
            receiveMessages((WebsocketReceive) action, aliasEnv, result);
        } else {
            subscribeToTopic((WebsocketSubscribe) action, aliasEnv, result);
        }
    }

    private void sendMessage(final WebsocketSend wsSend,
                             final AliasEnv aliasEnv,
                             final CommandResult result) throws IOException {
        final String message = getMessageToSend(wsSend);
        ResultUtil.addWebsocketInfoForSendAction(wsSend, aliasEnv.getAlias(), message, result);
        LogUtil.logWebsocketActionInfo(SEND_ACTION, wsSend.getComment(), wsSend.getEndpoint(), message);

        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        wsConnectionManager.sendMessage(wsSend, message);
    }

    private void receiveMessages(final WebsocketReceive wsReceive,
                                 final AliasEnv aliasEnv,
                                 final CommandResult result) {
        final String expectedContent = getExpectedContent(wsReceive);
        ResultUtil.addWebsocketInfoForReceiveAction(wsReceive, aliasEnv.getAlias(), result);
        LogUtil.logWebsocketActionInfo(RECEIVE_ACTION, wsReceive.getComment(), wsReceive.getTopic(), expectedContent);

        final List<Object> actualContent = getMessagesToCompare(wsReceive, aliasEnv);
        result.setActual(PrettifyStringJson.getJSONResult(toString(actualContent)));
        result.setExpected(PrettifyStringJson.getJSONResult(expectedContent));

        executeComparison(actualContent, expectedContent);
    }

    private List<Object> getMessagesToCompare(final WebsocketReceive wsReceive, final AliasEnv aliasEnv) {
        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        LinkedList<String> receivedMessages = wsConnectionManager.receiveMessages(wsReceive.getTopic());
        checkMessagesReceived(wsReceive, receivedMessages);

        int messageCount = wsReceive.getCount();
        if (messageCount == 0) {
            return Collections.unmodifiableList(receivedMessages);
        }
        return IntStream.range(0, messageCount)
                .mapToObj(id -> receivedMessages.pollFirst())
                .filter(Objects::nonNull)
                .map(this::toJsonObject)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private void checkMessagesReceived(final WebsocketReceive wsReceive,
                                       final LinkedList<String> receivedMessages) {
        int requiredMessageCount = wsReceive.getCount();
        if (requiredMessageCount > receivedMessages.size()) {
            TimeUnit.MILLISECONDS.sleep(wsReceive.getTimeoutMillis());
        }
    }

    private Object toJsonObject(final String content) {
        if ((content.startsWith(OPEN_BRACE) && content.endsWith(CLOSE_BRACE))
                || (content.startsWith(OPEN_SQUARE_BRACKET) && content.endsWith(CLOSE_SQUARE_BRACKET))) {
            return JacksonMapperUtil.readValue(content, Object.class);
        }
        return content;
    }

    private void executeComparison(final List<Object> actualContent,
                                   final String expectedContent) {
        CompareBuilder comparator = newCompare()
                .withExpected(expectedContent)
                .withActual(actualContent);
        comparator.exec();
    }

    private void subscribeToTopic(final WebsocketSubscribe wsSubscribe,
                                  final AliasEnv aliasEnv,
                                  final CommandResult result) {
        ResultUtil.addWebsocketInfoForSubscribeAction(wsSubscribe, aliasEnv.getAlias(), result);
        LogUtil.logWebsocketActionInfo(SUBSCRIBE, wsSubscribe.getComment(), wsSubscribe.getTopic(), EMPTY);
        wsConnectionSupplier.get(aliasEnv).subscribeTo(wsSubscribe.getTopic());
    }

    private String getMessageToSend(final WebsocketSend wsSend) {
        return getValue(wsSend.getMessage(), wsSend.getFile());
    }

    private String getExpectedContent(final WebsocketReceive wsReceive) {
        return getValue(wsReceive.getMessage(), wsReceive.getFile());
    }

    private String getValue(final String message, final String file) {
        return StringUtils.isNotBlank(message)
                ? message
                : FileSearcher.searchFileToString(file, dependencies.getFile());
    }

    @SneakyThrows
    private void disconnectIfEnabled(final boolean isDisconnectEnabled, final AliasEnv aliasEnv) {
        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        if (isDisconnectEnabled && wsConnectionManager.isConnected()) {
            wsConnectionManager.closeConnection();
        } else if (isDisconnectEnabled && !wsConnectionManager.isConnected()) {
            log.error(UNABLE_TO_DISCONNECT_BECAUSE_CONNECTION_CLOSED);
        }
    }
}
