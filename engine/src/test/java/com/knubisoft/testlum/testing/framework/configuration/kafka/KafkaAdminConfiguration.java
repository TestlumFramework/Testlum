package com.knubisoft.testlum.testing.framework.configuration.kafka;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
@RequiredArgsConstructor
public class KafkaAdminConfiguration {

    private final ConnectionTemplate connectionTemplate;

    private final Map<String, List<Kafka>> kafkaMap = GlobalTestConfigurationProvider.getIntegrations()
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().getKafkaIntegration().getKafka()));

    @Bean
    public Map<AliasEnv, KafkaAdmin> kafkaAdmin() {
        final Map<AliasEnv, KafkaAdmin> adminMap = new HashMap<>();
        kafkaMap.forEach((env, kafkaList) -> addKafkaAdmin(kafkaList, env, adminMap));
        return adminMap;
    }

    private void addKafkaAdmin(final List<Kafka> kafkaList,
                               final String env,
                               final Map<AliasEnv, KafkaAdmin> adminMap) {
        for (Kafka kafka : kafkaList) {
            if (kafka.isEnabled()) {
                KafkaAdmin kafkaAdmin = createKafkaAdmin(kafka);
                adminMap.put(new AliasEnv(kafka.getAlias(), env), kafkaAdmin);
            }
        }
    }

    private KafkaAdmin createKafkaAdmin(final Kafka kafka) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        return new KafkaAdmin(configs);
    }

    @Bean
    public Map<AliasEnv, AdminClient> kafkaAdminClient() {
        final Map<AliasEnv, AdminClient> clientMap = new HashMap<>();
        kafkaMap.forEach((env, kafkaList) -> addAdminClient(kafkaList, env, clientMap));
        return clientMap;
    }

    private void addAdminClient(final List<Kafka> kafkaList,
                                final String env,
                                final Map<AliasEnv, AdminClient> clientMap) {
        for (Kafka kafka : kafkaList) {
            if (kafka.isEnabled()) {
                AdminClient adminClient = createAdminClient(kafka);
                clientMap.put(new AliasEnv(kafka.getAlias(), env), adminClient);
            }
        }
    }

    private AdminClient createAdminClient(final Kafka kafka) {
        return connectionTemplate.executeWithRetry(
                String.format(CONNECTION_INTEGRATION_DATA, "Kafka Admin", kafka.getAlias()),
                () -> KafkaAdminClient.create(Map.of(
                        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress(),
                        AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000,
                        AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 10000
                )),
                HealthCheckFactory.forKafkaAdmin());
    }
}
