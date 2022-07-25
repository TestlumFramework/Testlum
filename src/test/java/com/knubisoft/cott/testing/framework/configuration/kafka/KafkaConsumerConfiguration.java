package com.knubisoft.cott.testing.framework.configuration.kafka;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
public class KafkaConsumerConfiguration {

    @Bean
    public Map<String, KafkaConsumer<String, String>> kafkaConsumer() {
        final Map<String, KafkaConsumer<String, String>> consumerMap = new HashMap<>();
        for (Kafka kafka : GlobalTestConfigurationProvider.getIntegrations().getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                createConsumerAndPutIntoMap(consumerMap, kafka);
            }
        }
        return consumerMap;
    }

    private void createConsumerAndPutIntoMap(final Map<String, KafkaConsumer<String, String>> consumerMap,
                                             final Kafka kafka) {
        final Properties props = new Properties();
        configureProperties(props, kafka);
        consumerMap.put(kafka.getAlias(), new KafkaConsumer<>(props));
    }

    private void configureProperties(final Properties props,
                                     final Kafka kafka) {
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        props.put(GROUP_ID_CONFIG, kafka.getGroupId());
        props.put(CLIENT_ID_CONFIG, kafka.getClientId());
        props.put(AUTO_OFFSET_RESET_CONFIG, kafka.getAutoOffsetReset());
        props.put(MAX_POLL_RECORDS_CONFIG, kafka.getMaxPollRecords());
        props.put(MAX_POLL_INTERVAL_MS_CONFIG, kafka.getMaxPollIntervalMs());
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    }
}
