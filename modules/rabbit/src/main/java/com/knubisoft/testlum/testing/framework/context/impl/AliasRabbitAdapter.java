package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.rabbitmq.RabbitMQOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
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
