package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.kafka.KafkaOperation;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.KAFKA;

@Conditional({OnKafkaEnabledCondition.class})
@Component
public class AliasKafkaAdapter implements AliasAdapter {

    @Autowired(required = false)
    private KafkaOperation kafkaOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Kafka kafka : GlobalTestConfigurationProvider.getDefaultIntegration().getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                aliasMap.put(KAFKA + UNDERSCORE + kafka.getAlias(), getMetadataKafka(kafka));
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
