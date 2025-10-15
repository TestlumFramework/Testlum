package com.knubisoft.testlum.testing.framework.interpreter;

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
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@InterpreterForClass(Kafka.class)
public class KafkaInterpreter extends AbstractInterpreter<Kafka> {

    // LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String NEW_LOG_LINE = format("%n%19s| ", EMPTY);
    private static final String CONTENT_FORMAT = format("%n%19s| %-23s|", EMPTY, EMPTY);
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001b[36m";
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String SEND_ACTION = "send";
    private static final String RECEIVE_ACTION = "receive";
    private static final String ACTION_LOG = format(TABLE_FORMAT, "Action", "{}");
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String CONTENT_LOG = format(TABLE_FORMAT, "Content", "{}");
    private static final String COMMIT_LOG = format(TABLE_FORMAT, "Commit", "{}");
    private static final String TOPIC_LOG = format(TABLE_FORMAT, "Topic", "{}");
    private static final String CORRELATION_ID_LOG = format(TABLE_FORMAT, "Correlation Id", "{}");
    private static final String TIMEOUT_MILLIS_LOG = format(TABLE_FORMAT, "Timeout Millis", "{}");
    private static final String COMMAND_LOG = ANSI_CYAN + "------- Command #{} - {} -------" + ANSI_RESET;
    private static final String EXCEPTION_LOG = ANSI_RED
            + "----------------    EXCEPTION    -----------------"
            + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
            + "--------------------------------------------------" + ANSI_RESET;

    //RESULT
    private static final String KEY = "Key";
    private static final String TOPIC = "Topic";
    private static final String ALIAS = "Alias";
    private static final String RECEIVE = "Receive";
    private static final String SEND = "Send";
    private static final String ACTION = "Action";
    private static final String ENABLE = "Enable";
    private static final String DISABLE = "Disable";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String MESSAGE_TO_SEND = "Message to send";
    private static final String TIMEOUT_MILLIS = "Timeout millis";
    private static final String HEADERS_STATUS = "Headers status";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String COMMENT_FOR_KAFKA_SEND_ACTION = "Send message to Kafka";
    private static final String COMMENT_FOR_KAFKA_RECEIVE_ACTION = "Receive message from Kafka";
    private static final String STEP_FAILED = "Step failed";


    private static final String CORRELATION_ID = "correlationId";
    private static final int NUM_PARTITIONS = 1;

    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, KafkaProducer<String, String>> kafkaProducer;
    @Autowired(required = false)
    private Map<AliasEnv, KafkaConsumer<String, String>> kafkaConsumer;
    @Autowired(required = false)
    private Map<AliasEnv, KafkaAdmin> kafkaAdmin;

    public KafkaInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Kafka o, final CommandResult result) {
        Kafka kafka = injectCommand(o);
        checkAlias(kafka);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (Object action : kafka.getSendOrReceive()) {
            log.info(COMMAND_LOG, dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
            CommandResult commandResult = newCommandResultInstance(dependencies.getPosition().get());
            subCommandsResult.add(commandResult);
            processEachAction(action, kafka.getAlias(), commandResult);
        }
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void checkAlias(final Kafka kafka) {
        if (kafka.getAlias() == null) {
            kafka.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        log.info(ALIAS_LOG, alias);
        try {
            processKafkaAction(action, alias, result);
        } catch (Exception e) {
            logException(e);
            setExceptionResult(result, e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void processKafkaAction(final Object action,
                                    final String alias,
                                    final CommandResult result) {
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof SendKafkaMessage) {
            sendMessage((SendKafkaMessage) action, aliasEnv, result);
        } else {
            receiveMessages((ReceiveKafkaMessage) action, aliasEnv, result);
        }
    }

    private void receiveMessages(final ReceiveKafkaMessage receive,
                                 final AliasEnv aliasEnv,
                                 final CommandResult result) {
        String expectedMessages = getMessageToReceive(receive);
        logKafkaReceiveInfo(receive, expectedMessages);
        addKafkaReceiveInfo(receive, aliasEnv.getAlias(), result);
        createTopicIfNotExists(receive.getTopic(), aliasEnv);
        List<KafkaMessage> actualMessages = receiveKafkaMessages(receive, aliasEnv);
        compareMessages(actualMessages, expectedMessages, result);
    }

    private void compareMessages(final List<KafkaMessage> actualKafkaMessages,
                                 final String value,
                                 final CommandResult result) {
        CompareBuilder comparator = newCompare()
                .withExpected(value)
                .withActual(actualKafkaMessages);
        result.setActual(StringPrettifier.asJsonResult(toString(actualKafkaMessages)));
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        comparator.exec();
    }

    private void sendMessage(final SendKafkaMessage send,
                             final AliasEnv aliasEnv,
                             final CommandResult result) {
        String message = getMessageToSend(send);
        logKafkaSendInfo(send, message);
        addKafkaSendInfo(send, aliasEnv.getAlias(), result);
        result.put(MESSAGE_TO_SEND, message);

        createTopicIfNotExists(send.getTopic(), aliasEnv);
        sendMessage(send, message, aliasEnv);
    }

    private void sendMessage(final SendKafkaMessage send,
                             final String value,
                             final AliasEnv aliasEnv) {
        ProducerRecord<String, String> producerRecord = buildProducerRecord(send, value);
        kafkaProducer.get(aliasEnv).send(producerRecord);
        kafkaProducer.get(aliasEnv).flush();
    }

    private List<KafkaMessage> receiveKafkaMessages(final ReceiveKafkaMessage receive, final AliasEnv aliasEnv) {
        Duration timeout = Duration.ofMillis(receive.getTimeoutMillis());
        KafkaConsumer<String, String> consumer = kafkaConsumer.get(aliasEnv);
        consumer.subscribe(Collections.singleton(receive.getTopic()));
        ConsumerRecords<String, String> consumerRecords = consumer.poll(timeout);
        Iterable<ConsumerRecord<String, String>> iterable = consumerRecords.records(receive.getTopic());
        Iterator<ConsumerRecord<String, String>> iterator = iterable.iterator();
        List<KafkaMessage> kafkaMessages = convertRecordsToKafkaMessages(iterator, receive);
        if (receive.isCommit()) {
            consumer.commitSync();
        }
        return kafkaMessages;
    }

    private List<KafkaMessage> convertRecordsToKafkaMessages(final Iterator<ConsumerRecord<String, String>> iterator,
                                                             final ReceiveKafkaMessage receive) {
        List<KafkaMessage> kafkaMessages = new ArrayList<>();
        while (iterator.hasNext()) {
            ConsumerRecord<String, String> consumerRecord = iterator.next();
            KafkaMessage kafkaMessage = createKafkaMessage(consumerRecord, receive.isHeaders());
            kafkaMessages.add(kafkaMessage);
        }
        return kafkaMessages;
    }

    private KafkaMessage createKafkaMessage(final ConsumerRecord<String, String> consumerRecord,
                                            final boolean isHeaders) {
        KafkaMessage kafkaMessage = new KafkaMessage(consumerRecord);
        if (isHeaders) {
            Map<String, String> headers = new HashMap<>();
            consumerRecord.headers().forEach(h -> headers.put(h.key(), new String(h.value(), StandardCharsets.UTF_8)));
            headers.remove(CORRELATION_ID);
            kafkaMessage.setHeaders(headers);
        }
        return kafkaMessage;
    }

    private ProducerRecord<String, String> buildProducerRecord(final SendKafkaMessage send, final String value) {
        return new ProducerRecord<>(send.getTopic(), null, send.getKey(), value, getHeaders(send));
    }

    private List<Header> getHeaders(final SendKafkaMessage send) {
        List<Header> headers = new ArrayList<>();
        if (nonNull(send.getCorrelationId())) {
            byte[] correlationId = send.getCorrelationId().getBytes(StandardCharsets.UTF_8);
            RecordHeader correlationIdHeader = new RecordHeader(CORRELATION_ID, correlationId);
            headers.add(correlationIdHeader);
        }
        headers.addAll(getHeadersFromSendMessage(send));
        return headers;
    }

    private List<Header> getHeadersFromSendMessage(final SendKafkaMessage send) {
        List<Header> headers = new ArrayList<>();
        if (nonNull(send.getHeaders()) && !CollectionUtils.isEmpty(send.getHeaders().getHeader())) {
            for (KafkaHeader kafkaHeader : send.getHeaders().getHeader()) {
                headers.add(convertToRecordHeader(kafkaHeader));
            }
        }
        return headers;
    }

    private Header convertToRecordHeader(final KafkaHeader kafkaHeader) {
        String headerName = kafkaHeader.getName();
        byte[] headerValue = kafkaHeader.getValue().getBytes(StandardCharsets.UTF_8);
        return new RecordHeader(headerName, headerValue);
    }

    private String getMessageToSend(final SendKafkaMessage send) {
        return getValue(send.getValue(), send.getFile());
    }

    private String getMessageToReceive(final ReceiveKafkaMessage receive) {
        return getValue(receive.getValue(), receive.getFile());
    }

    private String getValue(final String message, final String file) {
        return StringUtils.isNotBlank(message)
                ? message
                : getContentIfFile(file);
    }

    private void createTopicIfNotExists(final String topic, final AliasEnv aliasEnv) {
        if (checkIsTopicNotExists(topic, aliasEnv)) {
            NewTopic newTopic = new NewTopic(topic, NUM_PARTITIONS, (short) 1);
            kafkaAdmin.get(aliasEnv).createOrModifyTopics(newTopic);
        }
    }

    private boolean checkIsTopicNotExists(final String topic, final AliasEnv aliasEnv) {
        return kafkaConsumer.get(aliasEnv).listTopics().keySet().stream().noneMatch(topic::equals);
    }

    //LOGS
    private void logKafkaReceiveInfo(final ReceiveKafkaMessage receive, final String content) {
        logMessageBrokerGeneralMetaData(RECEIVE_ACTION, receive.getTopic(), content);
        logIfNotNull(TIMEOUT_MILLIS_LOG, receive.getTimeoutMillis());
        logIfNotNull(COMMIT_LOG, receive.isCommit());
    }

    private void logKafkaSendInfo(final SendKafkaMessage send, final String content) {
        logMessageBrokerGeneralMetaData(SEND_ACTION, send.getTopic(), content);
        logIfNotNull(CORRELATION_ID_LOG, send.getCorrelationId());
    }

    private void logMessageBrokerGeneralMetaData(final String action,
                                                 final String topicOrRoutingKeyOrQueueValue,
                                                 final String content) {
        log.info(ACTION_LOG, action.toUpperCase(Locale.ROOT));
        log.info(TOPIC_LOG, topicOrRoutingKeyOrQueueValue);
        log.info(CONTENT_LOG, StringPrettifier.asJsonResult(content.replaceAll(REGEX_MANY_SPACES, SPACE))
                .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
    }

    private void logIfNotNull(final String title, final Object data) {
        if (nonNull(data)) {
            log.info(title, data);
        }
    }

    private void logException(final Exception ex) {
        if (isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    //RESULT
    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }

    private void addKafkaSendInfo(final SendKafkaMessage sendAction,
                                  final String alias,
                                  final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_KAFKA_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, sendAction.getTopic(), result);
        addKafkaAdditionalMetaDataForSendAction(sendAction, result);
    }

    private void addKafkaReceiveInfo(final ReceiveKafkaMessage receiveAction,
                                     final String alias,
                                     final CommandResult result) {
        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_KAFKA_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, receiveAction.getTopic(), result);
        result.put(HEADERS_STATUS, receiveAction.isHeaders() ? ENABLE : DISABLE);
        result.put(TIMEOUT_MILLIS, receiveAction.getTimeoutMillis());
    }

    private void addKafkaAdditionalMetaDataForSendAction(final SendKafkaMessage sendAction,
                                                         final CommandResult result) {
        String key = sendAction.getKey();
        String correlationId = sendAction.getCorrelationId();
        KafkaHeaders kafkaHeaders = sendAction.getHeaders();
        if (isNotBlank(key)) {
            result.put(KEY, key);
        }
        if (isNotBlank(correlationId)) {
            result.put(CORRELATION_ID, correlationId);
        }
        if (nonNull(kafkaHeaders)) {
            result.put(ADDITIONAL_HEADERS, kafkaHeaders.getHeader().stream().map(header ->
                    format(HEADER_TEMPLATE, header.getName(), header.getValue())).collect(Collectors.toList()));
        }
    }
    private void addMessageBrokerGeneralMetaData(final String alias,
                                                 final String action,
                                                 final String destinationValue,
                                                 final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        result.put(TOPIC, destinationValue);
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
    private static class KafkaMessage {
        private final String key;
        private final Object value;
        private final String correlationId;
        private Map<String, String> headers;

        KafkaMessage(final ConsumerRecord<String, String> consumerRecord) {
            this.key = consumerRecord.key();
            this.value = JacksonMapperUtil.readValue(consumerRecord.value(), Object.class);
            this.correlationId = Optional.ofNullable(consumerRecord.headers().lastHeader(CORRELATION_ID))
                    .map(h -> new String(h.value(), StandardCharsets.UTF_8)).orElse(null);
        }
    }
}
