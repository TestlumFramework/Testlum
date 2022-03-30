package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.db.kafka.KafkaOperation;
import com.knubisoft.e2e.testing.model.global_config.Kafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnKafkaEnabledCondition.class})
@Component
public class AliasKafkaAdapter implements AliasAdapter {

    @Autowired(required = false)
    private KafkaOperation kafkaOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Kafka kafka : GlobalTestConfigurationProvider.getIntegrations().getKafkas().getKafka()) {
            if (kafka.isEnabled()) {
                aliasMap.put(kafka.getAlias(), getMetadataKafka(kafka));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataKafka(final Kafka kafka) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(kafka)
                .storageOperation(kafkaOperation)
                .build();
    }
}
