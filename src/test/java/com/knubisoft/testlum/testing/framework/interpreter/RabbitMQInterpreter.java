package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.PrettifyStringJson;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Rabbit;
import com.knubisoft.testlum.testing.model.scenario.ReceiveRmqMessage;
import com.knubisoft.testlum.testing.model.scenario.RmqHeader;
import com.knubisoft.testlum.testing.model.scenario.SendRmqMessage;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_LOG;
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
    @Autowired(required = false)
    private Map<AliasEnv, Client> rabbitMqClient;

    public RabbitMQInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Rabbit rabbit, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        int actionNumber = 1;
        for (Object action : rabbit.getSendOrReceive()) {
            CommandResult subCommandResult = ResultUtil.createNewCommandResultInstance(actionNumber);
            log.info(COMMAND_LOG, dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
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
            ResultUtil.setExceptionResult(subCommandResult, e);
            LogUtil.logException(e);
        } finally {
            subCommandResult.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void runRabbitMqOperation(final CommandResult subCommandResult,
                                      final Object action,
                                      final String alias) {
        log.info(ALIAS_LOG, alias);
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof SendRmqMessage) {
            SendRmqMessage sendAction = (SendRmqMessage) action;
            ResultUtil.addRabbitMQInfoForSendAction(sendAction, alias, subCommandResult);
            sendMessage((SendRmqMessage) action, subCommandResult, aliasEnv);
        } else {
            ReceiveRmqMessage receiveAction = (ReceiveRmqMessage) action;
            ResultUtil.addRabbitMQInfoForReceiveAction(receiveAction, alias, subCommandResult);
            receiveMessages((ReceiveRmqMessage) action, subCommandResult, aliasEnv);
        }
    }

    private void sendMessage(final SendRmqMessage send,
                             final CommandResult result,
                             final AliasEnv aliasEnv) {
        String message = getMessage(send);
        LogUtil.logBrokerActionInfo(SEND_ACTION, send.getRoutingKey(), message);
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
                                 final CommandResult result,
                                 final AliasEnv aliasEnv) {
        String message = getMessage(receive);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, receive.getQueue(), message);
        createQueueIfNotExists(receive.getQueue(), aliasEnv);
        List<RabbitMQMessage> actualRmqMessages = receiveRmqMessages(receive, aliasEnv);
        compareMessages(actualRmqMessages, message, result);
    }

    private List<RabbitMQMessage> receiveRmqMessages(final ReceiveRmqMessage receive, final AliasEnv aliasEnv) {
        List<RabbitMQMessage> actualRmqMessages = new ArrayList<>();
        for (int i = 0; i < receive.getPrefetchCount(); i++) {
            RabbitMQMessage actualRmqMessage = receiveRmqMessage(receive, aliasEnv);
            if (nonNull(actualRmqMessage)) {
                actualRmqMessages.add(actualRmqMessage);
            }
        }
        return actualRmqMessages;
    }

    private RabbitMQMessage receiveRmqMessage(final ReceiveRmqMessage receive, final AliasEnv aliasEnv) {
        Message actualMessage = rabbitTemplate.get(aliasEnv).receive(receive.getQueue(), receive.getTimeoutMillis());
        if (isNull(actualMessage)) {
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

    private void compareMessages(final List<RabbitMQMessage> actualRmqMessages,
                                 final String message,
                                 final CommandResult result) {
        CompareBuilder comparator = newCompare()
                .withExpected(message)
                .withActual(actualRmqMessages);
        result.setActual(PrettifyStringJson.getJSONResult(toString(actualRmqMessages)));
        result.setExpected(PrettifyStringJson.getJSONResult(comparator.getExpected()));
        comparator.exec();
    }

    private String getMessage(final SendRmqMessage send) {
        return isNull(send.getFile())
                ? send.getBody()
                : FileSearcher.searchFileToString(send.getFile(), dependencies.getFile());
    }

    private String getMessage(final ReceiveRmqMessage receive) {
        return isNull(receive.getFile())
                ? receive.getMessage()
                : FileSearcher.searchFileToString(receive.getFile(), dependencies.getFile());
    }

    private void createQueueIfNotExists(final String queue, final AliasEnv aliasEnv) {
        if (Objects.isNull(checkIfQueueExists(queue, aliasEnv))) {
            amqpAdmin.get(aliasEnv).declareQueue();
        }
    }

    private QueueInfo checkIfQueueExists(final String queue, final AliasEnv aliasEnv) {
        List<QueueInfo> queues = rabbitMqClient.get(aliasEnv).getQueues();
        return queues.stream()
                .filter(queueInfo -> queue.equals(queueInfo.getName()))
                .findFirst()
                .orElse(null);
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
