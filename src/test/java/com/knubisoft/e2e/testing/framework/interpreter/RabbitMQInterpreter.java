package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.Rabbit;
import com.knubisoft.e2e.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.model.scenario.RmqHeader;
import com.knubisoft.e2e.testing.model.scenario.SendRmqMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SEND_ACTION;

@Slf4j
@InterpreterForClass(Rabbit.class)
public class RabbitMQInterpreter extends AbstractInterpreter<Rabbit> {

    private static final String CORRELATION_ID = "correlationId";

    @Autowired(required = false)
    private Map<String, RabbitTemplate> rabbitTemplate;
    @Autowired(required = false)
    private Map<String, AmqpAdmin> amqpAdmin;

    public RabbitMQInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Rabbit rabbit, final CommandResult result) {
        int actionNumber = 0;
        for (Object action : rabbit.getSendOrReceive()) {
            runRabbitMqOperation(result, actionNumber, action, rabbit.getAlias());
            actionNumber++;
        }
    }

    private void runRabbitMqOperation(final CommandResult result,
                                      final int actionNumber,
                                      final Object action,
                                      final String alias) {
        log.info(ALIAS_LOG, alias,
                dependencies.getGlobalTestConfiguration().getIntegrations().getRabbitmqs().getRabbitmq()
                .stream().filter(a -> a.getAlias().equalsIgnoreCase(alias))
                .findFirst().get().getHost() + ":"
                        + dependencies.getGlobalTestConfiguration().getIntegrations().getRabbitmqs().getRabbitmq()
                        .stream().filter(a -> a.getAlias().equalsIgnoreCase(alias))
                        .findFirst().get().getPort());
        if (action instanceof SendRmqMessage) {
            sendMessage((SendRmqMessage) action, actionNumber, result, alias);
        } else {
            receiveMessages((ReceiveRmqMessage) action, actionNumber, result, alias);
        }
    }

    private void receiveMessages(final ReceiveRmqMessage receive,
                                 final int actionNumber,
                                 final CommandResult result,
                                 final String alias) {
        result.put("action[" + actionNumber + "]", RECEIVE_ACTION);
        result.put("queue[" + actionNumber + "]", receive.getQueue());

        String message = getMessage(receive);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, receive.getQueue(), message);

        createQueueIfNotExists(receive.getQueue(), alias);
        receiveMessages(receive, message, result, alias);
    }

    private void receiveMessages(final ReceiveRmqMessage receive,
                                 final String message,
                                 final CommandResult result,
                                 final String alias) {
        final List<RabbitMQMessage> actualRmqMessages = receiveRmqMessages(receive, alias);
        CompareBuilder comparator = newCompare()
                .withExpected(message)
                .withActual(actualRmqMessages);

        result.setActual(toString(actualRmqMessages));
        result.setExpected(comparator.getExpected());

        comparator.exec();
    }

    private void sendMessage(final SendRmqMessage send,
                             final int actionNumber,
                             final CommandResult result,
                             final String alias) {
        result.put("action[" + actionNumber + "]", SEND_ACTION);
        result.put("routingKey[" + actionNumber + "]", send.getRoutingKey());
        result.put("exchange[" + actionNumber + "]", send.getExchange());

        String message = getMessage(send);
        LogUtil.logBrokerActionInfo(SEND_ACTION, send.getExchange(), message);

        result.setActual(message);
        createQueueIfNotExists(send.getRoutingKey(), alias);
        sendMessage(send, message, alias);
    }

    private void sendMessage(final SendRmqMessage send, final String message, final String alias) {
        Message rmqMessage = buildRmqMessage(send, message, alias);
        rabbitTemplate.get(alias).send(send.getExchange(), send.getRoutingKey(), rmqMessage);
    }

    private List<RabbitMQMessage> receiveRmqMessages(final ReceiveRmqMessage receive,
                                                     final String alias) {
        List<RabbitMQMessage> actualRmqMessages = new ArrayList<>();

        for (int i = 0; i < receive.getPrefetchCount(); i++) {
            RabbitMQMessage actualRmqMessage = receiveRmqMessage(receive, alias);
            if (actualRmqMessage != null) {
                actualRmqMessages.add(actualRmqMessage);
            }
        }

        return actualRmqMessages;
    }

    private RabbitMQMessage receiveRmqMessage(final ReceiveRmqMessage receive,
                                              final String alias) {
        Message actualMessage = rabbitTemplate.get(alias).receive(receive.getQueue(), receive.getTimeoutMillis());
        if (actualMessage == null) {
            return null;
        }

        RabbitMQMessage actualRmqMessage = new RabbitMQMessage(actualMessage);
        setHeadersToActual(receive, actualMessage, actualRmqMessage);

        return actualRmqMessage;
    }

    private void setHeadersToActual(final ReceiveRmqMessage receive,
                                    final Message actualMessage,
                                    final RabbitMQMessage actualRmqMessage) {
        if (receive.isHeaders()) {
            actualRmqMessage.setHeaders(actualMessage.getMessageProperties().getHeaders());
            actualRmqMessage.getHeaders().remove(CORRELATION_ID);
        }
    }

    private Message buildRmqMessage(final SendRmqMessage send, final String message, final String alias) {
        MessageProperties properties = new MessageProperties();
        setHeaders(send, properties);
        setCorrelationId(send, properties);
        return rabbitTemplate.get(alias).getMessageConverter().toMessage(message, properties);
    }

    private void setCorrelationId(final SendRmqMessage send, final MessageProperties properties) {
        if (!StringUtils.isEmpty(send.getCorrelationId())) {
            properties.setHeader(CORRELATION_ID, send.getCorrelationId());
        }
    }

    private void setHeaders(final SendRmqMessage send, final MessageProperties properties) {
        if (send.getHeaders() != null) {
            for (RmqHeader rmqHeader : send.getHeaders().getHeader()) {
                properties.setHeader(rmqHeader.getName(), rmqHeader.getValue());
            }
        }
    }

    private String getMessage(final SendRmqMessage send) {
        return send.getFile() == null
                ? send.getBody()
                : dependencies.getFileSearcher().searchFileToString(send.getFile());
    }

    private String getMessage(final ReceiveRmqMessage receive) {
        return receive.getFile() == null
                ? receive.getMessage()
                : dependencies.getFileSearcher().searchFileToString(receive.getFile());
    }

    private void createQueueIfNotExists(final String queue,
                                        final String alias) {
        amqpAdmin.get(alias).declareQueue(new Queue(queue));
    }

    @Data
    private static class RabbitMQMessage {
        private final String message;
        private final String correlationId;
        private Map<String, Object> headers;

        RabbitMQMessage(final Message message) {
            this.message = new String(message.getBody(), StandardCharsets.UTF_8);

            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            this.correlationId = String.valueOf(headers.get(CORRELATION_ID));
        }
    }
}
