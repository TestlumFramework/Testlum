package com.knubisoft.testlum.testing.framework.db.rabbitmq;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.rabbitmq.http.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Conditional({OnRabbitMQEnabledCondition.class})
@Component
public class RabbitMQOperation extends StorageOperation {

    private final Map<AliasEnv, Client> rabbitMqClient;
    private final Map<String, Integrations> integrations;

    public RabbitMQOperation(@Autowired(required = false) @Qualifier("rabbitMqClient")
                             final Map<AliasEnv, Client> rabbitMqClient) {
        this.rabbitMqClient = rabbitMqClient;
        this.integrations = GlobalTestConfigurationProvider.getIntegrations();
    }

    @Override
    public StorageOperationResult apply(final Source source, final String name) {
        return null;
    }

    @Override
    public void clearSystem() {
        rabbitMqClient.forEach((aliasEnv, client) -> {
            if (isTruncate(Rabbitmq.class, aliasEnv)
                    && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                String virtualHost = this.findByName(aliasEnv).getVirtualHost();
                client.getQueues().forEach(queueInfo -> client.purgeQueue(virtualHost, queueInfo.getName()));
            }
        });
    }

    private Rabbitmq findByName(final AliasEnv aliasEnv) {
        List<Rabbitmq> rabbitmqs = integrations.get(aliasEnv.getEnvironment()).getRabbitmqIntegration().getRabbitmq();
        return IntegrationsUtil.findForAlias(rabbitmqs, aliasEnv.getAlias());
    }
}
