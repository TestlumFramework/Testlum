package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Kafka;
import com.knubisoft.testlum.testing.model.scenario.KafkaHeader;
import com.knubisoft.testlum.testing.model.scenario.ReceiveKafkaMessage;
import com.knubisoft.testlum.testing.model.scenario.SendKafkaMessage;
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

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.RECEIVE_ACTION;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SEND_ACTION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.MESSAGE_TO_SEND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@InterpreterForClass(Kafka.class)
public class KafkaInterpreter extends AbstractInterpreter<Kafka> {

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
    protected void acceptImpl(final Kafka kafka, final CommandResult commandResult) {
        List<CommandResult> subCommandsResultList = new LinkedList<>();
        int actionNumber = 1;
        for (Object action : kafka.getSendOrReceive()) {
            log.info(COMMAND_LOG, dependencies.getPosition().incrementAndGet(), action.getClass().getSimpleName());
            CommandResult result = ResultUtil.createNewCommandResultInstance(actionNumber);
            processEachAction(action, kafka.getAlias(), result);
            subCommandsResultList.add(result);
            actionNumber++;
        }
        commandResult.setSubCommandsResult(subCommandsResultList);
        ResultUtil.setExecutionResultIfSubCommandsFailed(commandResult);
    }

    private void processEachAction(final Object action,
                                   final String alias,
                                   final CommandResult subCommandResult) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processKafkaAction(subCommandResult, action, alias);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(subCommandResult, e);
        } finally {
            subCommandResult.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void processKafkaAction(final CommandResult subCommandResult,
                                    final Object action,
                                    final String alias) {
        log.info(ALIAS_LOG, alias);
        AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());
        if (action instanceof SendKafkaMessage) {
            SendKafkaMessage sendAction = (SendKafkaMessage) action;
            ResultUtil.addKafkaInfoForSendAction(sendAction, alias, subCommandResult);
            sendMessage(sendAction, subCommandResult, aliasEnv);
        } else {
            ReceiveKafkaMessage receiveAction = (ReceiveKafkaMessage) action;
            ResultUtil.addKafkaInfoForReceiveAction(receiveAction, alias, subCommandResult);
            receiveMessages(receiveAction, subCommandResult, aliasEnv);
        }
    }

    private void receiveMessages(final ReceiveKafkaMessage receive,
                                 final CommandResult result,
                                 final AliasEnv aliasEnv) {
        String value = getValue(receive);
        LogUtil.logBrokerActionInfo(RECEIVE_ACTION, receive.getTopic(), value);
        createTopicIfNotExists(receive.getTopic(), aliasEnv);
        List<KafkaMessage> actualMessages = receiveKafkaMessages(receive, aliasEnv);
        compareMessages(actualMessages, value, result);
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

    private void sendMessage(final SendKafkaMessage send, final CommandResult result, final AliasEnv aliasEnv) {
        String message = getValue(send);
        LogUtil.logBrokerActionInfo(SEND_ACTION, send.getTopic(), message);
        result.put(MESSAGE_TO_SEND, message);

        createTopicIfNotExists(send.getTopic(), aliasEnv);
        sendMessage(send, message, aliasEnv);
    }

    private void sendMessage(final SendKafkaMessage send, final String value, final AliasEnv aliasEnv) {
        ProducerRecord<String, String> producerRecord = buildProducerRecord(send, value);
        kafkaProducer.get(aliasEnv).send(producerRecord);
        kafkaProducer.get(aliasEnv).flush();
    }

    private List<KafkaMessage> receiveKafkaMessages(final ReceiveKafkaMessage receive, final AliasEnv aliasEnv) {
        Duration timeout = Duration.ofMillis(receive.getTimeoutMillis());

        kafkaConsumer.get(aliasEnv).subscribe(Collections.singleton(receive.getTopic()));
        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.get(aliasEnv).poll(timeout);

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

    private String getValue(final SendKafkaMessage send) {
        return isNull(send.getFile())
                ? send.getValue()
                : FileSearcher.searchFileToString(send.getFile(), dependencies.getFile());
    }

    private String getValue(final ReceiveKafkaMessage receive) {
        return isNull(receive.getFile())
                ? receive.getValue()
                : FileSearcher.searchFileToString(receive.getFile(), dependencies.getFile());
    }

    private void createTopicIfNotExists(final String topic, final AliasEnv aliasEnv) {
        NewTopic newTopic = new NewTopic(topic, NUM_PARTITIONS, (short) 1);
        kafkaAdmin.get(aliasEnv).createOrModifyTopics(newTopic);
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
