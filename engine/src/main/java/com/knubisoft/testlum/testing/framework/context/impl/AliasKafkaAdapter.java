package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.kafka.KafkaOperation;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.KAFKA;

@Conditional({OnKafkaEnabledCondition.class})
@Component
public class AliasKafkaAdapter implements AliasAdapter {

    @Autowired(required = false)
    private KafkaOperation kafkaOperation;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Kafka kafka : GlobalTestConfigurationProvider.get().getDefaultIntegrations().getKafkaIntegration().getKafka()) {
            if (kafka.isEnabled()) {
                aliasMap.put(KAFKA + UNDERSCORE + kafka.getAlias(), kafkaOperation);
            }
        }
    }
}
