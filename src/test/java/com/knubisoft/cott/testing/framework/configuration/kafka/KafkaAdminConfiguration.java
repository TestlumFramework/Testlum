package com.knubisoft.cott.testing.framework.configuration.kafka;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.Kafka;
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

@Configuration
@Conditional({OnKafkaEnabledCondition.class})
public class KafkaAdminConfiguration {

    private final Map<String, List<Kafka>> kafkaMap = GlobalTestConfigurationProvider.getIntegrations()
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().getKafkaIntegration().getKafka()));

    @Bean
    public Map<String, KafkaAdmin> kafkaAdmin() {
        final Map<String, KafkaAdmin> adminMap = new HashMap<>();
        kafkaMap.forEach((s, kafkaList) -> addKafkaAdmin(s, kafkaList, adminMap));
        return adminMap;
    }

    private void addKafkaAdmin(final String envName,
                               final List<Kafka> kafkaList,
                               final Map<String, KafkaAdmin> adminMap) {
        for (Kafka kafka : kafkaList) {
            if (kafka.isEnabled()) {
                createAdminAndPutIntoMap(adminMap, kafka, envName);
            }
        }
    }

    private void createAdminAndPutIntoMap(final Map<String, KafkaAdmin> adminMap,
                                          final Kafka kafka,
                                          final String envName) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        adminMap.put(envName + DelimiterConstant.UNDERSCORE + kafka.getAlias(),
                new KafkaAdmin(configs));
    }

    @Bean
    public Map<String, AdminClient> kafkaAdminClient() {
        final Map<String, AdminClient> clientMap = new HashMap<>();
        kafkaMap.forEach(((s, kafkaList) -> addAdminClient(clientMap, kafkaList, s)));
        return clientMap;
    }

    private void addAdminClient(final Map<String, AdminClient> clientMap,
                                final List<Kafka> kafkaList,
                                final String envName) {
        for (Kafka kafka : kafkaList) {
            if (kafka.isEnabled()) {
                createAdminClientAndPutIntoMap(clientMap, kafka, envName);
            }
        }
    }

    private void createAdminClientAndPutIntoMap(final Map<String, AdminClient> clientMap,
                                                final Kafka kafka,
                                                final String envName) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress());
        clientMap.put(envName + DelimiterConstant.UNDERSCORE + kafka.getAlias(),
                KafkaAdminClient.create(configs));
    }
}
