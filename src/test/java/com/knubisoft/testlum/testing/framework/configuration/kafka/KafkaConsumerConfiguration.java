package com.knubisoft.testlum.testing.framework.configuration.kafka;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
public class KafkaConsumerConfiguration {

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
        return new KafkaConsumer<>(props);
    }

    private void configureProperties(final Properties props, final Kafka kafka) {
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        props.put(GROUP_ID_CONFIG, kafka.getGroupId());
        props.put(CLIENT_ID_CONFIG, kafka.getClientId());
        props.put(AUTO_OFFSET_RESET_CONFIG, kafka.getAutoOffsetReset());
        props.put(MAX_POLL_RECORDS_CONFIG, kafka.getMaxPollRecords());
        props.put(MAX_POLL_INTERVAL_MS_CONFIG, kafka.getMaxPollIntervalMs());
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        if (Objects.nonNull(kafka.getAutoCommitTimeout()) && kafka.getAutoCommitTimeout() > 0) {
            props.put(ENABLE_AUTO_COMMIT_CONFIG, true);
            props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, kafka.getAutoCommitTimeout());
        }
    }
}
