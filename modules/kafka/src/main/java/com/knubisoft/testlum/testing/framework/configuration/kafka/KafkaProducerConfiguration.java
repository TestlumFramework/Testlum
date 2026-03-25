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

    private static final int TIME = 5;

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, KafkaProducer<String, String>> kafkaProducer(final EnvToIntegrationMap envToIntegrations) {
        Map<AliasEnv, KafkaProducer<String, String>> producerMap = new HashMap<>();
        envToIntegrations.forEach((env, integrations) -> addConfigProps(integrations, env, producerMap));
        return producerMap;
    }

    private void addConfigProps(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, KafkaProducer<String, String>> producerMap) {
        for (Kafka kafka : integrations.getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                KafkaProducer<String, String> checkedKafkaProducer = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "Kafka Producer", kafka.getAlias()),
                        () -> new KafkaProducer<>(createConfigProps(kafka)),
                        forKafkaProducer()
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

    private IntegrationHealthCheck<KafkaProducer<String, String>> forKafkaProducer() {
        return kafkaProducer -> kafkaProducer.clientInstanceId(Duration.ofSeconds(TIME));
    }
}
