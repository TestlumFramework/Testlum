package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractMessageBrokerInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Kafka;
import com.knubisoft.testlum.testing.model.scenario.KafkaHeader;
import com.knubisoft.testlum.testing.model.scenario.KafkaHeaders;
import com.knubisoft.testlum.testing.model.scenario.ReceiveKafkaMessage;
import com.knubisoft.testlum.testing.model.scenario.SendKafkaMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@InterpreterForClass(Kafka.class)
public class KafkaInterpreter extends AbstractMessageBrokerInterpreter<Kafka> {

    private static final String COMMIT_LOG = LogFormat.table("Commit");
    private static final String TOPIC_LOG = LogFormat.table("Topic");
    private static final String CORRELATION_ID_LOG = LogFormat.table("Correlation Id");
    private static final String TIMEOUT_MILLIS_LOG = LogFormat.table("Timeout Millis");

    private static final String KEY = "Key";
    private static final String TOPIC = "Topic";
    private static final String COMMENT_FOR_KAFKA_SEND_ACTION = "Send message to Kafka";
    private static final String COMMENT_FOR_KAFKA_RECEIVE_ACTION = "Receive message from Kafka";

    private static final String CORRELATION_ID = "correlationId";
    private static final int NUM_PARTITIONS = 1;

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
    protected String getAlias(final Kafka command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Kafka command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<Object> getActions(final Kafka command) {
        return command.getSendOrReceive();
    }

    @Override
    protected void processAction(final Object action, final String alias, final CommandResult result) {
        processKafkaAction(action, alias, result);
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
        if (Objects.nonNull(send.getCorrelationId())) {
            byte[] correlationId = send.getCorrelationId().getBytes(StandardCharsets.UTF_8);
            RecordHeader correlationIdHeader = new RecordHeader(CORRELATION_ID, correlationId);
            headers.add(correlationIdHeader);
        }
        headers.addAll(getHeadersFromSendMessage(send));
        return headers;
    }

    private List<Header> getHeadersFromSendMessage(final SendKafkaMessage send) {
        List<Header> headers = new ArrayList<>();
        if (Objects.nonNull(send.getHeaders()) && !CollectionUtils.isEmpty(send.getHeaders().getHeader())) {
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

    private void createTopicIfNotExists(final String topic, final AliasEnv aliasEnv) {
        if (checkIsTopicNotExists(topic, aliasEnv)) {
            NewTopic newTopic = new NewTopic(topic, NUM_PARTITIONS, (short) 1);
            kafkaAdmin.get(aliasEnv).createOrModifyTopics(newTopic);
        }
    }

    private boolean checkIsTopicNotExists(final String topic, final AliasEnv aliasEnv) {
        return kafkaConsumer.get(aliasEnv).listTopics().keySet().stream().noneMatch(topic::equals);
    }

    private void logKafkaReceiveInfo(final ReceiveKafkaMessage receive, final String content) {
        logMessageBrokerMetaData(RECEIVE_ACTION, TOPIC_LOG, receive.getTopic(), content);
        logIfNotNull(TIMEOUT_MILLIS_LOG, receive.getTimeoutMillis());
        logIfNotNull(COMMIT_LOG, receive.isCommit());
    }

    private void logKafkaSendInfo(final SendKafkaMessage send, final String content) {
        logMessageBrokerMetaData(SEND_ACTION, TOPIC_LOG, send.getTopic(), content);
        logIfNotNull(CORRELATION_ID_LOG, send.getCorrelationId());
    }

    private void addKafkaSendInfo(final SendKafkaMessage sendAction,
                                  final String alias,
                                  final CommandResult result) {
        result.setCommandKey(SEND);
        result.setComment(COMMENT_FOR_KAFKA_SEND_ACTION);
        addMessageBrokerGeneralMetaData(alias, SEND, TOPIC, sendAction.getTopic(), result);
        addKafkaAdditionalMetaDataForSendAction(sendAction, result);
    }

    private void addKafkaReceiveInfo(final ReceiveKafkaMessage receiveAction,
                                     final String alias,
                                     final CommandResult result) {
        result.setCommandKey(RECEIVE);
        result.setComment(COMMENT_FOR_KAFKA_RECEIVE_ACTION);
        addMessageBrokerGeneralMetaData(alias, RECEIVE, TOPIC, receiveAction.getTopic(), result);
        result.put(HEADERS_STATUS, receiveAction.isHeaders() ? ENABLE : DISABLE);
        result.put(TIMEOUT_MILLIS, receiveAction.getTimeoutMillis());
    }

    private void addKafkaAdditionalMetaDataForSendAction(final SendKafkaMessage sendAction,
                                                         final CommandResult result) {
        String key = sendAction.getKey();
        String correlationId = sendAction.getCorrelationId();
        KafkaHeaders kafkaHeaders = sendAction.getHeaders();
        if (StringUtils.isNotBlank(key)) {
            result.put(KEY, key);
        }
        if (StringUtils.isNotBlank(correlationId)) {
            result.put(CORRELATION_ID, correlationId);
        }
        if (Objects.nonNull(kafkaHeaders)) {
            result.put(ADDITIONAL_HEADERS, kafkaHeaders.getHeader().stream().map(header ->
                    String.format(HEADER_TEMPLATE, header.getName(), header.getValue())).toList());
        }
    }

    @Data
    private class KafkaMessage {
        private final String key;
        private final Object value;
        private final String correlationId;
        private Map<String, String> headers;

        KafkaMessage(final ConsumerRecord<String, String> consumerRecord) {
            this.key = consumerRecord.key();
            this.value = jacksonService.readValue(consumerRecord.value(), Object.class);
            this.correlationId = Optional.ofNullable(consumerRecord.headers().lastHeader(CORRELATION_ID))
                    .map(h -> new String(h.value(), StandardCharsets.UTF_8)).orElse(null);
        }
    }
}
