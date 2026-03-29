package com.knubisoft.testlum.testing.framework.db.kafka;

import com.knubisoft.testlum.testing.framework.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Conditional({OnKafkaEnabledCondition.class})
@Component
public class KafkaOperation extends AbstractStorageOperation {

    private static final int TIMEOUT = 10;

    private final Map<AliasEnv, KafkaConsumer<String, String>> kafkaConsumer;
    private final Map<AliasEnv, AdminClient> adminClient;

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

    private void clearKafka(final KafkaConsumer<String, String> kafkaConsumer, final AliasEnv aliasEnv) {
        Set<String> userTopics = getUserTopics(kafkaConsumer);
        if (!userTopics.isEmpty()) {
            deleteTopics(aliasEnv, userTopics);
        }
    }

    private Set<String> getUserTopics(final KafkaConsumer<String, String> kafkaConsumer) {
        return kafkaConsumer.listTopics().keySet().stream()
                .filter(topic -> !topic.startsWith("_"))
                .collect(Collectors.toSet());
    }

    private void deleteTopics(final AliasEnv aliasEnv, final Set<String> userTopics) {
        try {
            adminClient.get(aliasEnv).deleteTopics(userTopics).all().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DefaultFrameworkException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new DefaultFrameworkException(e);
        }
    }
}
