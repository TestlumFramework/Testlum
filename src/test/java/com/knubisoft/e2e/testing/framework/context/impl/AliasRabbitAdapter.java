package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.db.rabbitmq.RabbitMQOperation;
import com.knubisoft.e2e.testing.model.global_config.Rabbitmq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnRabbitMQEnabledCondition.class})
@Component
public class AliasRabbitAdapter implements AliasAdapter {

    @Autowired(required = false)
    private RabbitMQOperation rabbitMQOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Rabbitmq rabbitmq : GlobalTestConfigurationProvider.getIntegrations().getRabbitmqs().getRabbitmq()) {
            if (rabbitmq.isEnabled()) {
                aliasMap.put(rabbitmq.getAlias(), getMetadataRabbit(rabbitmq));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataRabbit(final Rabbitmq rabbitmq) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(rabbitmq)
                .storageOperation(rabbitMQOperation)
                .build();
    }
}
