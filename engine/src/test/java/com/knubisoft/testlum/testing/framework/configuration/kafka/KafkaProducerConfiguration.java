package com.knubisoft.testlum.testing.framework.configuration.kafka;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
@RequiredArgsConstructor
public class KafkaProducerConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, KafkaProducer<String, String>> kafkaProducer() {
        Map<AliasEnv, KafkaProducer<String, String>> producerMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addConfigProps(integrations, env, producerMap));
        return producerMap;
    }

    private void addConfigProps(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, KafkaProducer<String, String>> producerMap) {
        for (Kafka kafka : integrations.getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                KafkaProducer<String, String> checkedKafkaProducer = connectionTemplate.executeWithRetry(
                        "Kafka Producer - " + kafka.getAlias(),
                        () -> {
                            KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(createConfigProps(kafka));
                            try {
                                kafkaProducer.clientInstanceId(Duration.ofSeconds(5));
                                return kafkaProducer;
                            } catch (Exception e) {
                                kafkaProducer.close();
                                if (e.getMessage() == null) {
                                    throw new DefaultFrameworkException(e.getClass().getSimpleName());
                                }
                                throw new DefaultFrameworkException(e.getMessage());
                            }
                        }
                );
                producerMap.put(new AliasEnv(kafka.getAlias(), env), checkedKafkaProducer);
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
