package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.rabbitmq.RabbitMQOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnRabbitMQEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasRabbitAdapter implements AliasAdapter {

    private final RabbitMQOperation rabbitMQOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Rabbitmq rabbitmq : integrations.getRabbitmqIntegration().getRabbitmq()) {
            if (rabbitmq.isEnabled()) {
                aliasMap.put("Rabbitmq" + DelimiterConstant.UNDERSCORE + rabbitmq.getAlias(), rabbitMQOperation);
            }
        }
    }
}
