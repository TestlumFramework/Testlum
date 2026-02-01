package com.knubisoft.testlum.testing.framework.db.kafka;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Conditional({OnKafkaEnabledCondition.class})
@Component
public class KafkaOperation extends AbstractStorageOperation {

    private static final int TIMEOUT = 10;

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
            if (isTruncate(Kafka.class, aliasEnv)
                    && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                clearKafka(kafkaConsumer, aliasEnv);
            }
        });
    }

    @SneakyThrows
    private void clearKafka(final KafkaConsumer<String, String> kafkaConsumer, final AliasEnv aliasEnv) {
        Set<String> userTopics = kafkaConsumer.listTopics().keySet().stream()
                .filter(topic -> !topic.startsWith("_"))
                .collect(Collectors.toSet());
        if (userTopics.isEmpty()) {
            return;
        }

        DeleteTopicsResult deleteTopicsResult = adminClient.get(aliasEnv).deleteTopics(userTopics);
        deleteTopicsResult.all().get(TIMEOUT, TimeUnit.SECONDS);
    }
}
