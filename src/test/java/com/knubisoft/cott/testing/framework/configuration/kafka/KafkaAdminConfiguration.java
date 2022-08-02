package com.knubisoft.cott.testing.framework.configuration.kafka;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
public class KafkaAdminConfiguration {

    @Bean
    public Map<String, KafkaAdmin> kafkaAdmin() {
        final Map<String, KafkaAdmin> adminMap = new HashMap<>();
        for (Kafka kafka : GlobalTestConfigurationProvider.getIntegrations().getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                createAdminAndPutIntoMap(adminMap, kafka);
            }
        }
        return adminMap;
    }

    private void createAdminAndPutIntoMap(final Map<String, KafkaAdmin> adminMap, final Kafka kafka) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        adminMap.put(kafka.getAlias(), new KafkaAdmin(configs));
    }

    @Bean
    public Map<String, AdminClient> kafkaAdminClient() {
        final Map<String, AdminClient> clientMap = new HashMap<>();
        for (Kafka kafka : GlobalTestConfigurationProvider.getIntegrations().getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                createAdminClientAndPutIntoMap(clientMap, kafka);
            }
        }
        return clientMap;
    }

    private void createAdminClientAndPutIntoMap(final Map<String, AdminClient> clientMap, final Kafka kafka) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        clientMap.put(kafka.getAlias(), KafkaAdminClient.create(configs));
    }
}
