package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.KafkaHeader;
import com.knubisoft.cott.testing.model.scenario.SendKafkaMessage;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.scenario.Kafka;
import com.knubisoft.cott.testing.model.scenario.ReceiveKafkaMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.util.LogMessage.ALIAS_LOG;
import static com.knubisoft.cott.testing.framework.util.LogMessage.EXCEPTION_LOG;
import static com.knubisoft.cott.testing.framework.util.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.cott.testing.framework.util.LogMessage.SEND_ACTION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.MESSAGE_TO_SEND;

@Slf4j
@InterpreterForClass(Kafka.class)
public class KafkaInterpreter extends AbstractInterpreter<Kafka> {

    private static final String CORRELATION_ID = "correlationId";
    private static final int NUM_PARTITIONS = 1;

    @Autowired(required = false)
    private Map<String, KafkaProducer<String, String>> kafkaProducer;
    @Autowired(required = false)
    private Map<String, KafkaConsumer<String, String>> kafkaConsumer;
    @Autowired(required = false)
    private Map<String, KafkaAdmin> kafkaAdmin;

    public KafkaInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Kafka kafka, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        int actionNumber = 1;
        for (Object action : kafka.getSendOrReceive()) {
            CommandResult subCommandResult = ResultUtil.createNewCommandResultInstance(actionNumber);
            processEachAction(action, kafka.getAlias(), subCommandResult);
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
            processKafkaAction(subCommandResult, action, alias);
        } catch (Exception e) {
            subCommandResult.setSuccess(false);
            subCommandResult.setException(e);
            log.error(EXCEPTION_LOG, e.getMessage());
        } finally {
            subCommandResult.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void processKafkaAction(final CommandResult subCommandResult,
                                   final Object action,
                                   final String alias) {
        log.info(ALIAS_LOG, alias);
        if (action instanceof SendKafkaMessage) {
            SendKafkaMessage sendAction = (SendKafkaMessage) action;
            ResultUtil.addKafkaInfoForSendAction(sendAction, alias, subCommandResult);
            sendMessage(sendAction, subCommandResult, alias);
        } else {
            ReceiveKafkaMessage receiveAction = (ReceiveKafkaMessage) action;
            ResultUtil.addKafkaInfoForReceiveAction(receiveAction, alias, subCommandResult);
            receiveMessages(receiveAction, subCommandResult, alias);
        }
    }

    private void receiveMessages(final ReceiveKafkaMessage receive,
                                 final CommandResult result,
                                 final String alias) {
        String value = getValue(receive);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, receive.getTopic(), value);

        kafkaAdmin.get(alias).createOrModifyTopics(new NewTopic(receive.getTopic(), 1, (short) 1));
        receiveMessages(receive, value, result, alias);
    }

    private void receiveMessages(final ReceiveKafkaMessage receive,
                                 final String value,
                                 final CommandResult result,
                                 final String alias) {
        createTopicIfNotExists(receive.getTopic(), alias);

        List<KafkaMessage> actualKafkaMessages = receiveKafkaMessages(receive, alias);
        CompareBuilder comparator = newCompare()
                .withExpected(value)
                .withActual(actualKafkaMessages);

        result.setActual(PrettifyStringJson.getJSONResult(toString(actualKafkaMessages)));
        result.setExpected(PrettifyStringJson.getJSONResult(comparator.getExpected()));

        comparator.exec();
    }

    private void sendMessage(final SendKafkaMessage send,
                             final CommandResult result,
                             final String alias) {
        String message = getValue(send);
        LogUtil.logBrokerActionInfo(SEND_ACTION, send.getTopic(), message);

        result.put(MESSAGE_TO_SEND, message);
        createTopicIfNotExists(send.getTopic(), alias);

        sendMessage(send, message, alias);
    }

    private void sendMessage(final SendKafkaMessage send, final String value, final String alias) {
        ProducerRecord<String, String> producerRecord = buildProducerRecord(send, value);
        kafkaProducer.get(alias).send(producerRecord);
        kafkaProducer.get(alias).flush();
    }

    private List<KafkaMessage> receiveKafkaMessages(final ReceiveKafkaMessage receive,
                                                    final String alias) {
        Duration timeout = Duration.ofMillis(receive.getTimeoutMillis());

        kafkaConsumer.get(alias).subscribe(Collections.singleton(receive.getTopic()));
        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.get(alias).poll(timeout);

        Iterable<ConsumerRecord<String, String>> iterable = consumerRecords.records(receive.getTopic());
        Iterator<ConsumerRecord<String, String>> iterator = iterable.iterator();

        return convertConsumerRecordsToKafkaMessages(iterator);
    }

    private List<KafkaMessage> convertConsumerRecordsToKafkaMessages(
            final Iterator<ConsumerRecord<String, String>> iterator) {
        List<KafkaMessage> kafkaMessages = new ArrayList<>();

        while (iterator.hasNext()) {
            ConsumerRecord<String, String> consumerRecord = iterator.next();
            KafkaMessage kafkaMessage = new KafkaMessage(consumerRecord);
            kafkaMessages.add(kafkaMessage);
        }

        return kafkaMessages;
    }

    private ProducerRecord<String, String> buildProducerRecord(final SendKafkaMessage send, final String value) {
        return new ProducerRecord<>(send.getTopic(), null, send.getKey(), value, getHeaders(send));
    }

    private List<Header> getHeaders(final SendKafkaMessage send) {
        List<Header> headers = new ArrayList<>();
        if (send.getCorrelationId() != null) {
            byte[] correlationId = send.getCorrelationId().getBytes(StandardCharsets.UTF_8);
            RecordHeader correlationIdHeader = new RecordHeader(CORRELATION_ID, correlationId);
            headers.add(correlationIdHeader);
        }
        headers.addAll(getHeadersFromSendMessage(send));
        return headers;
    }

    private List<Header> getHeadersFromSendMessage(final SendKafkaMessage send) {
        List<Header> headers = new ArrayList<>();

        if (send.getHeaders() != null && !CollectionUtils.isEmpty(send.getHeaders().getHeader())) {
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

    private String getValue(final SendKafkaMessage send) {
        return send.getFile() == null
                ? send.getValue()
                : FileSearcher.searchFileToString(send.getFile(), dependencies.getFile());
    }

    private String getValue(final ReceiveKafkaMessage receive) {
        return receive.getFile() == null
                ? receive.getValue()
                : FileSearcher.searchFileToString(receive.getFile(), dependencies.getFile());
    }

    private void createTopicIfNotExists(final String topic, final String alias) {
        NewTopic newTopic = new NewTopic(topic, NUM_PARTITIONS, (short) 1);
        kafkaAdmin.get(alias).createOrModifyTopics(newTopic);
    }

    @Data
    private static class KafkaMessage {
        private final String key;
        private final String value;
        private final String correlationId;
        private final Map<String, String> headers;

        KafkaMessage(final ConsumerRecord<String, String> consumerRecord) {
            this.key = consumerRecord.key();
            this.value = consumerRecord.value();
            Header[] headerArray = consumerRecord.headers().toArray();
            this.headers = Arrays.stream(headerArray).collect(
                    Collectors.toMap(Header::key, h -> new String(h.value(), StandardCharsets.UTF_8)));
            this.correlationId = headers.get(CORRELATION_ID);
            this.headers.remove(CORRELATION_ID);
        }
    }
}
