package com.knubisoft.cott.testing.framework.db.kafka;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaOperation implements StorageOperation {

    private static final int THREAD_SLEEPING_MILLIS = 10;

    private final Map<String, KafkaConsumer<String, String>> kafkaConsumer;
    private final Map<String, AdminClient> adminClient;

    public KafkaOperation(@Autowired(required = false) final Map<String, KafkaConsumer<String, String>> kafkaConsumer,
                          @Autowired(required = false) final Map<String, AdminClient> adminClient) {
        this.kafkaConsumer = kafkaConsumer;
        this.adminClient = adminClient;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    @SneakyThrows
    public void clearSystem() {
        for (Kafka kafka : GlobalTestConfigurationProvider.getIntegrations().getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                clearKafka(kafka);
            }
        }

    }

    private void clearKafka(final Kafka kafka) throws InterruptedException {
        Map<String, List<PartitionInfo>> topics = kafkaConsumer.get(kafka.getAlias()).listTopics();
        Set<String> topicsName = topics.keySet();
        DeleteTopicsResult deleteTopicsResult = adminClient.get(kafka.getAlias()).deleteTopics(topicsName);
        while (!deleteTopicsResult.all().isDone()) {
            TimeUnit.MILLISECONDS.sleep(THREAD_SLEEPING_MILLIS);
        }
    }
}
