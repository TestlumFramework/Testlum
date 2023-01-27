package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.db.kafka.KafkaOperation;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnKafkaEnabledCondition.class})
@Component
public class AliasKafkaAdapter implements AliasAdapter {

    @Autowired(required = false)
    private KafkaOperation kafkaOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getKafkaIntegration().getKafka(),
                        aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Kafka> kafkaList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Kafka kafka : kafkaList) {
            if (kafka.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + kafka.getAlias(), getMetadataKafka(kafka));
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
