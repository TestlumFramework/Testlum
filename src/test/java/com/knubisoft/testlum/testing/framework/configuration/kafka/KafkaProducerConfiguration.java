package com.knubisoft.testlum.testing.framework.configuration.kafka;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
public class KafkaProducerConfiguration {

    @Bean
    public Map<AliasEnv, KafkaProducer<String, String>> kafkaProducer() {
        Map<AliasEnv, KafkaProducer<String, String>> producerMap = new HashMap<>();
        ConfigProvider.getIntegrations().forEach((env, integrations) -> addConfigProps(integrations, env, producerMap));
        return producerMap;
    }

    private void addConfigProps(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, KafkaProducer<String, String>> producerMap) {
        for (Kafka kafka : integrations.getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(createConfigProps(kafka));
                producerMap.put(new AliasEnv(kafka.getAlias(), env), kafkaProducer);
            }
        }
    }

    private Map<String, Object> createConfigProps(final Kafka kafka) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return configProps;
    }
}
