package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.rabbitmq.RabbitMQOperation;
import com.knubisoft.cott.testing.model.global_config.Rabbitmq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnRabbitMQEnabledCondition.class})
@Component
public class AliasRabbitAdapter implements AliasAdapter {

    @Autowired(required = false)
    private RabbitMQOperation rabbitMQOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getRabbitmqIntegration().getRabbitmq(),
                        aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Rabbitmq> rabbitmqList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Rabbitmq rabbitmq : rabbitmqList) {
            if (rabbitmq.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + rabbitmq.getAlias(), getMetadataRabbit(rabbitmq));
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
