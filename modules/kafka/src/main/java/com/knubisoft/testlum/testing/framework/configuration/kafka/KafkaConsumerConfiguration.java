package com.knubisoft.testlum.testing.framework.configuration.kafka;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {

    private static final int TIMEOUT = 5000;
    private static final int TIME = 5;

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, KafkaConsumer<String, String>>
    kafkaConsumer(final EnvToIntegrationMap envToIntegrations) {
        final Map<AliasEnv, KafkaConsumer<String, String>> consumerMap = new HashMap<>();
        envToIntegrations.forEach((env, integration) -> addKafkaConsumer(integration, env, consumerMap));
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
                String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "Kafka Consumer", kafka.getAlias()),
                () -> new KafkaConsumer<>(props),
                forKafkaConsumer()
        );
    }

    private IntegrationHealthCheck<KafkaConsumer<String, String>> forKafkaConsumer() {
        return kafkaConsumer -> kafkaConsumer.listTopics(Duration.ofSeconds(TIME));
    }

    private void configureProperties(final Properties props, final Kafka kafka) {
        configureBaseProperties(props, kafka);
        configureAutoCommit(props, kafka);
    }

    private void configureBaseProperties(final Properties props, final Kafka kafka) {
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafka.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafka.getClientId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafka.getAutoOffsetReset());
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, TIMEOUT);
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, TIMEOUT);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafka.getMaxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafka.getMaxPollIntervalMs());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    }

    private void configureAutoCommit(final Properties props, final Kafka kafka) {
        if (Objects.nonNull(kafka.getAutoCommitTimeout()) && kafka.getAutoCommitTimeout() > 0) {
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, kafka.getAutoCommitTimeout());
        }
    }
}
