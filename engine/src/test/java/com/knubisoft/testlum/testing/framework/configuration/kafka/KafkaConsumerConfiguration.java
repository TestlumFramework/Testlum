package com.knubisoft.testlum.testing.framework.configuration.kafka;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, KafkaConsumer<String, String>> kafkaConsumer() {
        final Map<AliasEnv, KafkaConsumer<String, String>> consumerMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addKafkaConsumer(integrations, env, consumerMap));
        return consumerMap;
    }

    private void addKafkaConsumer(final Integrations integrations,
                                  final String env,
                                  final Map<AliasEnv, KafkaConsumer<String, String>> consumerMap) {
        for (Kafka kafka : integrations.getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer(kafka);
                consumerMap.put(new AliasEnv(kafka.getAlias(), env), kafkaConsumer);
            }
        }
    }

    private KafkaConsumer<String, String> createKafkaConsumer(final Kafka kafka) {
        final Properties props = new Properties();
        configureProperties(props, kafka);
        return connectionTemplate.executeWithRetry(
                "Kafka Consumer - " + kafka.getAlias(),
                () -> {
                    KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
                    try {
                        kafkaConsumer.listTopics(Duration.ofSeconds(5));
                        return kafkaConsumer;
                    } catch (Exception e) {
                        kafkaConsumer.close();
                        if (e.getMessage() == null) {
                            throw new DefaultFrameworkException(e.getClass().getSimpleName());
                        }
                        throw new DefaultFrameworkException(e.getMessage());
                    }
                }
        );
    }

    private void configureProperties(final Properties props, final Kafka kafka) {
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        props.put(GROUP_ID_CONFIG, kafka.getGroupId());
        props.put(CLIENT_ID_CONFIG, kafka.getClientId());
        props.put(AUTO_OFFSET_RESET_CONFIG, kafka.getAutoOffsetReset());
        props.put(REQUEST_TIMEOUT_MS_CONFIG, 5000);
        props.put(DEFAULT_API_TIMEOUT_MS_CONFIG, 5000);
        props.put(MAX_POLL_RECORDS_CONFIG, kafka.getMaxPollRecords());
        props.put(MAX_POLL_INTERVAL_MS_CONFIG, kafka.getMaxPollIntervalMs());
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        if (Objects.nonNull(kafka.getAutoCommitTimeout()) && kafka.getAutoCommitTimeout() > 0) {
            props.put(ENABLE_AUTO_COMMIT_CONFIG, true);
            props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, kafka.getAutoCommitTimeout());
        }
    }
}
