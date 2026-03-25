package com.knubisoft.testlum.testing.framework.configuration.kafka;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
@RequiredArgsConstructor
public class KafkaAdminConfiguration {

    private static final int TIMEOUT = 10000;
    private static final int TIME = 5;

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<String, List<Kafka>> getKafkaMap(final EnvToIntegrationMap envToIntegrations) {
        return envToIntegrations.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getKafkaIntegration().getKafka()));
    }

    @Bean
    public Map<AliasEnv, KafkaAdmin> kafkaAdmin(final Map<String, List<Kafka>> kafkaMap) {
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
    public Map<AliasEnv, AdminClient> kafkaAdminClient(final Map<String, List<Kafka>> kafkaMap) {
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
                String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "Kafka Admin", kafka.getAlias()),
                () -> KafkaAdminClient.create(Map.of(
                        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress(),
                        AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, TIMEOUT,
                        AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, TIMEOUT
                )),
                forKafkaAdmin());
    }

    private IntegrationHealthCheck<AdminClient> forKafkaAdmin() {
        return client -> client.listTopics().names().get(TIME, TimeUnit.SECONDS);
    }
}
