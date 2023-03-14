package com.knubisoft.cott.testing.framework.db.kafka;

import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.model.AliasEnv;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Conditional({OnKafkaEnabledCondition.class})
@Component
public class KafkaOperation implements StorageOperation {

    private static final int THREAD_SLEEPING_MILLIS = 10;

    private final Map<AliasEnv, KafkaConsumer<String, String>> kafkaConsumer;
    private final Map<AliasEnv, AdminClient> adminClient;

    public KafkaOperation(@Autowired(required = false) final Map<AliasEnv, KafkaConsumer<String, String>> kafkaConsumer,
                          @Autowired(required = false) final Map<AliasEnv, AdminClient> adminClient) {
        this.kafkaConsumer = kafkaConsumer;
        this.adminClient = adminClient;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
        kafkaConsumer.forEach((aliasEnv, kafkaConsumer) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                clearKafka(kafkaConsumer, aliasEnv);
            }
        });
    }

    @SneakyThrows
    private void clearKafka(final KafkaConsumer<String, String> kafkaConsumer, final AliasEnv aliasEnv) {
        Map<String, List<PartitionInfo>> topics = kafkaConsumer.listTopics();
        Set<String> topicsName = topics.keySet();
        DeleteTopicsResult deleteTopicsResult = adminClient.get(aliasEnv).deleteTopics(topicsName);
        while (!deleteTopicsResult.all().isDone()) {
            TimeUnit.MILLISECONDS.sleep(THREAD_SLEEPING_MILLIS);
        }
    }
}
