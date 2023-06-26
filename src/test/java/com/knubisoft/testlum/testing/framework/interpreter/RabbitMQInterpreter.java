package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Rabbit;
import com.knubisoft.testlum.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.testlum.testing.model.scenario.RmqHeader;
import com.knubisoft.testlum.testing.model.scenario.SendRmqMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.MESSAGE_TO_SEND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Rabbit.class)
public class RabbitMQInterpreter extends AbstractInterpreter<Rabbit> {

    private static final String CORRELATION_ID = "correlationId";

    @Autowired(required = false)
    private Map<AliasEnv, RabbitTemplate> rabbitTemplate;
    @Autowired(required = false)
    private Map<AliasEnv, AmqpAdmin> amqpAdmin;

    public RabbitMQInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Rabbit o, final CommandResult result) {
        Rabbit rabbit = injectCommand(o);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        final AtomicInteger commandId = new AtomicInteger();
        for (Object action : rabbit.getSendOrReceive()) {
            LogUtil.logSubCommand(dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
            CommandResult commandResult = ResultUtil.newCommandResultInstance(commandId.incrementAndGet());
            subCommandsResult.add(commandResult);
            processEachAction(action, rabbit.getAlias(), commandResult);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        LogUtil.logAlias(alias);
        try {
            runRabbitMqOperation(action, alias, result);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(result, e);
            LogUtil.logException(e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void runRabbitMqOperation(final Object action,
                                      final String alias,
                                      final CommandResult result) {
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof SendRmqMessage) {
            sendMessage((SendRmqMessage) action, aliasEnv, result);
        } else {
            receiveMessages((ReceiveRmqMessage) action, aliasEnv, result);
        }
    }

    private void sendMessage(final SendRmqMessage send,
                             final AliasEnv aliasEnv,
                             final CommandResult result) {
        String message = getMessageToSend(send);
        LogUtil.logBrokerActionInfo(SEND_ACTION, send.getRoutingKey(), message);
        ResultUtil.addRabbitMQInfoForSendAction(send, aliasEnv.getAlias(), result);
        result.put(MESSAGE_TO_SEND, message);
        createQueueIfNotExists(send.getRoutingKey(), aliasEnv);
        sendMessage(send, message, aliasEnv);
    }

    private void sendMessage(final SendRmqMessage send, final String message, final AliasEnv aliasEnv) {
        Message rmqMessage = buildRmqMessage(send, message, aliasEnv);
        rabbitTemplate.get(aliasEnv).send(send.getExchange(), send.getRoutingKey(), rmqMessage);
    }

    private Message buildRmqMessage(final SendRmqMessage send, final String message, final AliasEnv aliasEnv) {
        MessageProperties properties = new MessageProperties();
        setHeaders(send, properties);
        setCorrelationId(send, properties);
        return rabbitTemplate.get(aliasEnv).getMessageConverter().toMessage(message, properties);
    }

    private void setHeaders(final SendRmqMessage send, final MessageProperties properties) {
        if (nonNull(send.getHeaders())) {
            for (RmqHeader rmqHeader : send.getHeaders().getHeader()) {
                properties.setHeader(rmqHeader.getName(), rmqHeader.getValue());
            }
        }
    }

    private void setCorrelationId(final SendRmqMessage send, final MessageProperties properties) {
        if (StringUtils.isNotBlank(send.getCorrelationId())) {
            properties.setHeader(CORRELATION_ID, send.getCorrelationId());
        }
    }

    private void receiveMessages(final ReceiveRmqMessage receive,
                                 final AliasEnv aliasEnv,
                                 final CommandResult result) {
        String messages = getMessageToReceive(receive);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, receive.getQueue(), messages);
        ResultUtil.addRabbitMQInfoForReceiveAction(receive, aliasEnv.getAlias(), result);
        createQueueIfNotExists(receive.getQueue(), aliasEnv);
        List<RabbitMQMessage> actualRmqMessages = receiveRmqMessages(receive, aliasEnv);
        compareMessages(actualRmqMessages, messages, result);
    }

    private List<RabbitMQMessage> receiveRmqMessages(final ReceiveRmqMessage receive, final AliasEnv aliasEnv) {
        return IntStream.range(0, receive.getPrefetchCount())
                .mapToObj(i -> receiveRmqMessage(receive, aliasEnv))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private RabbitMQMessage receiveRmqMessage(final ReceiveRmqMessage receive, final AliasEnv aliasEnv) {
        Message actualMessage = rabbitTemplate.get(aliasEnv).receive(receive.getQueue(), receive.getTimeoutMillis());
        if (isNull(actualMessage)) {
            return null;
        }
        return convertToRMQMessage(actualMessage, receive);
    }

    private RabbitMQMessage convertToRMQMessage(final Message actualMessage, final ReceiveRmqMessage receive) {
        RabbitMQMessage actualRmqMessage = new RabbitMQMessage(actualMessage);
        if (receive.isHeaders()) {
            actualRmqMessage.setHeaders(actualMessage.getMessageProperties().getHeaders());
            actualRmqMessage.getHeaders().remove(CORRELATION_ID);
        }
        return actualRmqMessage;
    }

    private void compareMessages(final List<RabbitMQMessage> actualRmqMessages,
                                 final String message,
                                 final CommandResult result) {
        CompareBuilder comparator = newCompare()
                .withExpected(message)
                .withActual(actualRmqMessages);
        result.setActual(StringPrettifier.asJsonResult(toString(actualRmqMessages)));
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        comparator.exec();
    }

    private String getMessageToSend(final SendRmqMessage send) {
        return getValue(send.getBody(), send.getFile());
    }

    private String getMessageToReceive(final ReceiveRmqMessage receive) {
        return getValue(receive.getMessage(), receive.getFile());
    }

    private String getValue(final String message, final String file) {
        return StringUtils.isNotBlank(message)
                ? message
                : getContentIfFile(file);
    }

    private void createQueueIfNotExists(final String queue, final AliasEnv aliasEnv) {
        if (checkIsQueueNotExists(queue, aliasEnv)) {
            amqpAdmin.get(aliasEnv).declareQueue(new Queue(queue));
        }
    }

    private boolean checkIsQueueNotExists(final String queue, final AliasEnv aliasEnv) {
        return isNull(amqpAdmin.get(aliasEnv).getQueueProperties(queue));
    }

    @Data
    private static class RabbitMQMessage {
        private final String message;
        private final String correlationId;
        private Map<String, Object> headers;

        RabbitMQMessage(final Message message) {
            this.message = new String(message.getBody(), StandardCharsets.UTF_8).replaceAll("\\n\\s+", "");
            Object header = message.getMessageProperties().getHeader(CORRELATION_ID);
            this.correlationId = String.valueOf(header);
        }
    }
}
