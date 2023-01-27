package com.knubisoft.cott.testing.framework.configuration.kafka;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
public class KafkaProducerConfiguration {

    private final Map<String, List<Kafka>> kafkaMap = GlobalTestConfigurationProvider.getIntegrations()
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().getKafkaIntegration().getKafka()));

    @Bean
    public Map<String, KafkaProducer<String, String>> kafkaProducer() {
        Map<String, KafkaProducer<String, String>> producerMap = new HashMap<>();
        kafkaMap.forEach(((s, kafkaList) -> addConfigProps(s, kafkaList, producerMap)));
        return producerMap;
    }

    private void addConfigProps(final String envName,
                                final List<Kafka> kafkaList,
                                final Map<String, KafkaProducer<String, String>> producerMap) {
        for (Kafka kafka : kafkaList) {
            if (kafka.isEnabled()) {
                Map<String, Object> configProps = createConfigProps(kafka);
                producerMap.put(envName + DelimiterConstant.UNDERSCORE + kafka.getAlias(),
                        new KafkaProducer<>(configProps));
            }
        }
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
