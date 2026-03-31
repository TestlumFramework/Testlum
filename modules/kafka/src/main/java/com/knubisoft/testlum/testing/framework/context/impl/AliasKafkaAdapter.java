package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnKafkaEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.kafka.KafkaOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnKafkaEnabledCondition.class})
@Component
public class AliasKafkaAdapter extends AbstractAliasAdapter {

    public AliasKafkaAdapter(final KafkaOperation kafkaOperation,
                             final Integrations integrations) {
        super(kafkaOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getKafkaIntegration().getKafka();
    }

    @Override
    protected String getStorageName() {
        return "Kafka";
    }
}
