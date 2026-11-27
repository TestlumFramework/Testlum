package com.testlum.testing.framework.context.impl;

import com.testlum.testing.framework.condition.OnRabbitMQEnabledCondition;
import com.testlum.testing.framework.context.AbstractAliasAdapter;
import com.testlum.testing.framework.db.rabbitmq.RabbitMQOperation;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnRabbitMQEnabledCondition.class})
@Component
public class AliasRabbitAdapter extends AbstractAliasAdapter {

    public AliasRabbitAdapter(final RabbitMQOperation rabbitMQOperation,
                              final Integrations integrations) {
        super(rabbitMQOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getRabbitmqIntegration().getRabbitmq();
    }

    @Override
    protected String getStorageName() {
        return "Rabbitmq";
    }
}
