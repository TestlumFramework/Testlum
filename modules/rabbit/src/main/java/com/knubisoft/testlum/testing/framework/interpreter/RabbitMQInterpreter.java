package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractMessageBrokerInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Rabbit;
import com.knubisoft.testlum.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.testlum.testing.model.scenario.RmqHeader;
import com.knubisoft.testlum.testing.model.scenario.RmqHeaders;
import com.knubisoft.testlum.testing.model.scenario.SendRmqMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

@Slf4j
@InterpreterForClass(Rabbit.class)
public class RabbitMQInterpreter extends AbstractMessageBrokerInterpreter<Rabbit> {

    private static final String QUEUE_LOG = LogFormat.table("Queue");
    private static final String ROUTING_KEY_LOG = LogFormat.table("Routing Key");
    private static final String PREFETCH_COUNT_LOG = LogFormat.table("Prefetch Count");
    private static final String CORRELATION_ID_LOG = LogFormat.table("Correlation Id");
    private static final String TIMEOUT_MILLIS_LOG = LogFormat.table("Timeout Millis");

    private static final String QUEUE = "Queue";
    private static final String EXCHANGE = "Exchange";
    private static final String ROUTING_KEY = "Routing Key";
    private static final String COMMENT_FOR_RABBIT_SEND_ACTION = "Send message to RabbitMQ";
    private static final String COMMENT_FOR_RABBIT_RECEIVE_ACTION = "Receive message from RabbitMQ";

    private static final String QUEUE_DOES_NOT_EXIST = "Queue with name <%s> does not exist";
    private static final String CORRELATION_ID = "correlationId";

    @Autowired(required = false)
    private Map<AliasEnv, RabbitTemplate> rabbitTemplate;
    @Autowired(required = false)
    private Map<AliasEnv, AmqpAdmin> amqpAdmin;

    public RabbitMQInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected String getAlias(final Rabbit command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Rabbit command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<Object> getActions(final Rabbit command) {
        return command.getSendOrReceive();
    }

    @Override
    protected void processAction(final Object action, final String alias, final CommandResult result) {
        runRabbitMqOperation(action, alias, result);
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
        logRabbitSendInfo(send, message);
        addRabbitMQSendInfo(send, aliasEnv.getAlias(), result);
        result.put(MESSAGE_TO_SEND, message);
        checkQueueExistence(send.getRoutingKey(), aliasEnv);
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
        if (Objects.nonNull(send.getHeaders())) {
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
        logRabbitReceiveInfo(receive, messages);
        addRabbitMQReceiveInfo(receive, aliasEnv.getAlias(), result);
        checkQueueExistence(receive.getQueue(), aliasEnv);
        List<RabbitMQMessage> actualRmqMessages = receiveRmqMessages(receive, aliasEnv);
        compareMessages(actualRmqMessages, messages, result);
    }

    private List<RabbitMQMessage> receiveRmqMessages(final ReceiveRmqMessage receive, final AliasEnv aliasEnv) {
        return IntStream.range(0, receive.getPrefetchCount())
                .mapToObj(i -> receiveRmqMessage(receive, aliasEnv))
                .filter(Objects::nonNull)
                .toList();
    }

    private RabbitMQMessage receiveRmqMessage(final ReceiveRmqMessage receive, final AliasEnv aliasEnv) {
        Message actualMessage = rabbitTemplate.get(aliasEnv).receive(receive.getQueue(), receive.getTimeoutMillis());
        if (Objects.isNull(actualMessage)) {
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

    private String getMessageToSend(final SendRmqMessage send) {
        return getValue(send.getValue(), send.getFile());
    }

    private String getMessageToReceive(final ReceiveRmqMessage receive) {
        return getValue(receive.getValue(), receive.getFile());
    }

    private void checkQueueExistence(final String queue, final AliasEnv aliasEnv) {
        if (Objects.isNull(amqpAdmin.get(aliasEnv).getQueueProperties(queue))) {
            throw new DefaultFrameworkException(QUEUE_DOES_NOT_EXIST, queue);
        }
    }

    private void logRabbitSendInfo(final SendRmqMessage send, final String content) {
        logMessageBrokerMetaData(SEND_ACTION, ROUTING_KEY_LOG, send.getRoutingKey(), content);
        logIfNotNull(CORRELATION_ID_LOG, send.getCorrelationId());
    }

    private void logRabbitReceiveInfo(final ReceiveRmqMessage receive, final String content) {
        logMessageBrokerMetaData(RECEIVE_ACTION, QUEUE_LOG, receive.getQueue(), content);
        logIfNotNull(TIMEOUT_MILLIS_LOG, receive.getTimeoutMillis());
        logIfNotNull(PREFETCH_COUNT_LOG, receive.getPrefetchCount());
    }

    private void addRabbitMQSendInfo(final SendRmqMessage sendAction,
                                     final String alias,
                                     final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_RABBIT_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, ROUTING_KEY, sendAction.getRoutingKey(), result);
        addRabbitMQAdditionalMetaDataForSendAction(sendAction, result);
    }

    private void addRabbitMQAdditionalMetaDataForSendAction(final SendRmqMessage sendAction,
                                                            final CommandResult result) {
        String exchange = sendAction.getExchange();
        String correlationId = sendAction.getCorrelationId();
        RmqHeaders rabbitHeaders = sendAction.getHeaders();
        if (StringUtils.isNotBlank(exchange)) {
            result.put(EXCHANGE, exchange);
        }
        if (StringUtils.isNotBlank(correlationId)) {
            result.put(CORRELATION_ID, correlationId);
        }
        if (Objects.nonNull(rabbitHeaders)) {
            result.put(ADDITIONAL_HEADERS, rabbitHeaders.getHeader().stream().map(header ->
                    String.format(HEADER_TEMPLATE, header.getName(), header.getValue())).toList());
        }
    }

    private void addRabbitMQReceiveInfo(final ReceiveRmqMessage receiveAction,
                                        final String alias,
                                        final CommandResult result) {
        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_RABBIT_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, QUEUE, receiveAction.getQueue(), result);
        result.put(HEADERS_STATUS, receiveAction.isHeaders() ? ENABLE : DISABLE);
        result.put(TIMEOUT_MILLIS, receiveAction.getTimeoutMillis());
    }

    @Data
    private class RabbitMQMessage {
        private final Object message;
        private final String correlationId;
        private Map<String, Object> headers;

        RabbitMQMessage(final Message message) {
            this.message = jacksonService.readValue(message.getBody(), Object.class);
            Object header = message.getMessageProperties().getHeader(CORRELATION_ID);
            this.correlationId = String.valueOf(header);
        }
    }
}
