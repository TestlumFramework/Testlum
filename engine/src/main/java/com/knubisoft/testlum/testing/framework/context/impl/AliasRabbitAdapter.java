package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.rabbitmq.RabbitMQOperation;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.RABBITMQ;

@Conditional({OnRabbitMQEnabledCondition.class})
@Component
public class AliasRabbitAdapter implements AliasAdapter {

    @Autowired(required = false)
    private RabbitMQOperation rabbitMQOperation;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Rabbitmq rabbitmq
                : GlobalTestConfigurationProvider.getDefaultIntegrations().getRabbitmqIntegration().getRabbitmq()) {
            if (rabbitmq.isEnabled()) {
                aliasMap.put(RABBITMQ + UNDERSCORE + rabbitmq.getAlias(), rabbitMQOperation);
            }
        }
    }
}
