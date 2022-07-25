package com.knubisoft.cott.testing.framework.configuration.kafka;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
public class KafkaProducerConfiguration {

    @Bean
    public Map<String, KafkaProducer<String, String>> kafkaProducer() {
        Map<String, KafkaProducer<String, String>> producerMap = new HashMap<>();
        for (Kafka kafka : GlobalTestConfigurationProvider.getIntegrations().getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                Map<String, Object> configProps = createConfigProps(kafka);
                producerMap.put(kafka.getAlias(), new KafkaProducer<>(configProps));
            }
        }
        return producerMap;
    }

    @NotNull
    private Map<String, Object> createConfigProps(final Kafka kafka) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return configProps;
    }
}
