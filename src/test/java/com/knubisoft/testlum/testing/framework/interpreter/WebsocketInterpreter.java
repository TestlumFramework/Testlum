package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.configuration.websocket.WebsocketConnectionManager;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Websocket;
import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSubscribe;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.CLOSE_BRACE;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.CLOSE_SQUARE_BRACKET;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.WEBSOCKET_CONNECTION_FAILURE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SUBSCRIBE;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@InterpreterForClass(Websocket.class)
public class WebsocketInterpreter extends AbstractInterpreter<Websocket> {

    private static final int ALL_AVAILABLE_MESSAGES = 0;
    private static final int CHECK_PERIOD_MS = 100;

    @Autowired(required = false)
    private Map<AliasEnv, WebsocketConnectionManager> wsConnectionSupplier;

    public WebsocketInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Websocket o, final CommandResult result) {
        Websocket websocket = injectCommand(o, Websocket.class);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        processWebsockets(websocket, subCommandsResult);
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processWebsockets(final Websocket websocket, final List<CommandResult> subCommandsResult) {
        AliasEnv aliasEnv = new AliasEnv(websocket.getAlias(), dependencies.getEnvironment());
        openConnection(aliasEnv);
        runActions(websocket, subCommandsResult);
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

    private void runActions(final Websocket websocket, final List<CommandResult> subCommandsResult) {
        List<Object> websocketActions = isNull(websocket.getStomp())
                ? websocket.getSendOrReceive()
                : websocket.getStomp().getSubscribeOrSendOrReceive();
        websocketActions.forEach(action -> {
            LogUtil.logSubCommand(dependencies.getPosition().incrementAndGet(), action);
            CommandResult commandResult = ResultUtil.newCommandResultInstance(dependencies.getPosition().get());
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
            ResultUtil.setExceptionResult(result, e);
            LogUtil.logException(e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
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
                .map(this::toJsonObject)
                .collect(Collectors.toList());
    }

    private void checkMessagesAreReceived(final int requiredMessageCount,
                                          final long timeoutMillis,
                                          final LinkedList<String> receivedMessages) {
        if (requiredMessageCount <= ALL_AVAILABLE_MESSAGES && timeoutMillis > 0) {
            WaitUtil.sleep(timeoutMillis, TimeUnit.MILLISECONDS);
        }
        if (requiredMessageCount > receivedMessages.size() && timeoutMillis > 0) {
            WaitUtil.waitUntil(() -> receivedMessages.size() >= requiredMessageCount,
                    timeoutMillis, TimeUnit.MILLISECONDS, CHECK_PERIOD_MS);
        }
    }

    private Object toJsonObject(final String content) {
        if (isNotBlank(content)
                && ((content.startsWith(OPEN_BRACE) && content.endsWith(CLOSE_BRACE))
                || (content.startsWith(OPEN_SQUARE_BRACKET) && content.endsWith(CLOSE_SQUARE_BRACKET)))) {
            return JacksonMapperUtil.readValue(content, Object.class);
        }
        return content;
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
        return isNotBlank(message)
                ? message
                : FileSearcher.searchFileToString(file, dependencies.getFile());
    }

    @SneakyThrows
    private void disconnectIfEnabled(final boolean isDisconnectEnabled, final AliasEnv aliasEnv) {
        WebsocketConnectionManager wsConnectionManager = wsConnectionSupplier.get(aliasEnv);
        if (isDisconnectEnabled) {
            wsConnectionManager.closeConnection();
        }
    }
}
