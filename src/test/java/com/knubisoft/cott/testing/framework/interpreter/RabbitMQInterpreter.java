package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Rabbit;
import com.knubisoft.cott.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.scenario.RmqHeader;
import com.knubisoft.cott.testing.model.scenario.SendRmqMessage;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.EXCEPTION_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.MESSAGE_TO_SEND;

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
        List<CommandResult> subCommandsResult = new LinkedList<>();
        int actionNumber = 1;
        for (Object action : rabbit.getSendOrReceive()) {
            CommandResult subCommandResult = ResultUtil.createNewCommandResultInstance(actionNumber);
            processEachAction(action, rabbit.getAlias(), subCommandResult);
            subCommandsResult.add(subCommandResult);
            actionNumber++;
        }
        result.setSubCommandsResult(subCommandsResult);
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult subCommandResult) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            runRabbitMqOperation(subCommandResult, action, alias);
        } catch (Exception e) {
            subCommandResult.setSuccess(false);
            subCommandResult.setException(e);
            log.error(EXCEPTION_LOG, e.getMessage());
        } finally {
            subCommandResult.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void runRabbitMqOperation(final CommandResult subCommandResult,
                                      final Object action,
                                      final String alias) {
        log.info(ALIAS_LOG, alias);
        if (action instanceof SendRmqMessage) {
            SendRmqMessage sendAction = (SendRmqMessage) action;
            ResultUtil.addRabbitMQInfoForSendAction(sendAction, alias, subCommandResult);
            sendMessage((SendRmqMessage) action, subCommandResult, alias);
        } else {
            ReceiveRmqMessage receiveAction = (ReceiveRmqMessage) action;
            ResultUtil.addRabbitMQInfoForReceiveAction(receiveAction, alias, subCommandResult);
            receiveMessages((ReceiveRmqMessage) action, subCommandResult, alias);
        }
    }

    private void receiveMessages(final ReceiveRmqMessage receive,
                                 final CommandResult result,
                                 final String alias) {
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

        result.setActual(PrettifyStringJson.getJSONResult(toString(actualRmqMessages)));
        result.setExpected(PrettifyStringJson.getJSONResult(comparator.getExpected()));

        comparator.exec();
    }

    private void sendMessage(final SendRmqMessage send,
                             final CommandResult result,
                             final String alias) {
        String message = getMessage(send);
        LogUtil.logBrokerActionInfo(SEND_ACTION, send.getRoutingKey(), message);

        result.put(MESSAGE_TO_SEND, message);
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
                : FileSearcher.searchFileToString(send.getFile(), dependencies.getFile());
    }

    private String getMessage(final ReceiveRmqMessage receive) {
        return receive.getFile() == null
                ? receive.getMessage()
                : FileSearcher.searchFileToString(receive.getFile(), dependencies.getFile());
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
