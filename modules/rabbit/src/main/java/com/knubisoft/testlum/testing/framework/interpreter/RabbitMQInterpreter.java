package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;

@Slf4j
@InterpreterForClass(Rabbit.class)
public class RabbitMQInterpreter extends AbstractInterpreter<Rabbit> {

    //LOGS
    private static final String NEW_LOG_LINE = String.format("%n%19s| ", StringUtils.EMPTY);
    private static final String CONTENT_FORMAT = String.format("%n%19s| %-23s|", StringUtils.EMPTY, StringUtils.EMPTY);

    private static final String SEND_ACTION = "send";
    private static final String RECEIVE_ACTION = "receive";
    private static final String QUEUE_LOG = LogFormat.table("Queue");
    private static final String ACTION_LOG = LogFormat.table("Action");
    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String CONTENT_LOG = LogFormat.table("Content");
    private static final String ROUTING_KEY_LOG = LogFormat.table("Routing Key");
    private static final String PREFETCH_COUNT_LOG = LogFormat.table("Prefetch Count");
    private static final String CORRELATION_ID_LOG = LogFormat.table("Correlation Id");
    private static final String TIMEOUT_MILLIS_LOG = LogFormat.table("Timeout Millis");
    private static final String COMMAND_LOG = LogFormat.withCyan("------- Command #{} - {} -------");
    private static final String EXCEPTION_LOG = LogFormat.withRed(
            "----------------    EXCEPTION    -----------------"
            + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
            + "--------------------------------------------------");

    //RESULT
    private static final String MESSAGE_TO_SEND = "Message to send";
    private static final String ALIAS = "Alias";
    private static final String QUEUE = "Queue";
    private static final String RECEIVE = "Receive";
    private static final String SEND = "Send";
    private static final String ACTION = "Action";
    private static final String ENABLE = "Enable";
    private static final String DISABLE = "Disable";
    private static final String EXCHANGE = "Exchange";
    private static final String ROUTING_KEY = "Routing Key";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String TIMEOUT_MILLIS = "Timeout millis";
    private static final String HEADERS_STATUS = "Headers status";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String COMMENT_FOR_RABBIT_SEND_ACTION = "Send message to RabbitMQ";
    private static final String COMMENT_FOR_RABBIT_RECEIVE_ACTION = "Receive message from RabbitMQ";
    private static final String STEP_FAILED = "Step failed";

    private static final String QUEUE_DOES_NOT_EXIST = "Queue with name <%s> does not exist";

    private static final String CORRELATION_ID = "correlationId";

    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

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
        checkAlias(rabbit);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (Object action : rabbit.getSendOrReceive()) {
            log.info(COMMAND_LOG, dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
            CommandResult commandResult = newCommandResultInstance(dependencies.getPosition().get());
            subCommandsResult.add(commandResult);
            processEachAction(action, rabbit.getAlias(), commandResult);
        }
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void checkAlias(final Rabbit rabbit) {
        if (rabbit.getAlias() == null) {
            rabbit.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        log.info(ALIAS_LOG, alias);
        try {
            runRabbitMqOperation(action, alias, result);
        } catch (Exception e) {
            setExceptionResult(result, e);
            logException(e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getDuration().toMillis());
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
                .collect(Collectors.toList());
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
        return getValue(send.getValue(), send.getFile());
    }

    private String getMessageToReceive(final ReceiveRmqMessage receive) {
        return getValue(receive.getValue(), receive.getFile());
    }

    private String getValue(final String message, final String file) {
        return StringUtils.isNotBlank(message)
                ? message
                : getContentIfFile(file);
    }

    private void checkQueueExistence(final String queue, final AliasEnv aliasEnv) {
        if (Objects.isNull(amqpAdmin.get(aliasEnv).getQueueProperties(queue))) {
            throw new DefaultFrameworkException(QUEUE_DOES_NOT_EXIST, queue);
        }
    }

    //LOGS
    private void logRabbitSendInfo(final SendRmqMessage send, final String content) {
        logMessageBrokerGeneralMetaData(SEND_ACTION, ROUTING_KEY_LOG, send.getRoutingKey(), content);
        logIfNotNull(CORRELATION_ID_LOG, send.getCorrelationId());
    }

    private void logRabbitReceiveInfo(final ReceiveRmqMessage receive, final String content) {
        logMessageBrokerGeneralMetaData(RECEIVE_ACTION, QUEUE_LOG, receive.getQueue(), content);
        logIfNotNull(TIMEOUT_MILLIS_LOG, receive.getTimeoutMillis());
        logIfNotNull(PREFETCH_COUNT_LOG, receive.getPrefetchCount());
    }

    private void logMessageBrokerGeneralMetaData(final String action,
                                                 final String topicOrRoutingKeyOrQueue,
                                                 final String topicOrRoutingKeyOrQueueValue,
                                                 final String content) {
        log.info(ACTION_LOG, action.toUpperCase(Locale.ROOT));
        log.info(topicOrRoutingKeyOrQueue, topicOrRoutingKeyOrQueueValue);
        log.info(CONTENT_LOG, StringPrettifier.asJsonResult(content.replaceAll(REGEX_MANY_SPACES, SPACE))
                .replaceAll(LogFormat.newLine(), CONTENT_FORMAT));
    }

    private void logIfNotNull(final String title, final Object data) {
        if (Objects.nonNull(data)) {
            log.info(title, data);
        }
    }

    private void logException(final Exception ex) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(LogFormat.newLine(), NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    //RESULT
    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (Objects.nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
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

    private void addMessageBrokerGeneralMetaData(final String alias,
                                                 final String action,
                                                 final String destination,
                                                 final String destinationValue,
                                                 final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        result.put(destination, destinationValue);
    }

    private void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult
                    .stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    private void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    @Data
    private static class RabbitMQMessage {
        private final Object message;
        private final String correlationId;
        private Map<String, Object> headers;

        RabbitMQMessage(final Message message) {
            this.message = JacksonMapperUtil.readValue(message.getBody(), Object.class);
            Object header = message.getMessageProperties().getHeader(CORRELATION_ID);
            this.correlationId = String.valueOf(header);
        }
    }
}
