package com.knubisoft.cott.testing.framework.db.rabbitmq;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Rabbitmq;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Component
public class RabbitMQOperation implements StorageOperation {

    private final Map<String, Client> client;
    private final Map<String, Integrations> integrations;

    public RabbitMQOperation(@Autowired(required = false) @Qualifier("rabbitMqClient")
                             final Map<String, Client> client) {
        this.client = client;
        this.integrations = GlobalTestConfigurationProvider.getIntegrations();
    }

    @Override
    public StorageOperationResult apply(final Source source, final String name) {
        return null;
    }

    @Override
    public void clearSystem() {
        for (Map.Entry<String, Client> entry : client.entrySet()) {
            List<QueueInfo> queueInfoList = entry.getValue().getQueues();
            String virtualHost = this.findByName(entry.getKey()).getVirtualHost();
            for (QueueInfo queueInfo : queueInfoList) {
                entry.getValue().deleteQueue(virtualHost, queueInfo.getName());
            }
        }
    }

    private Rabbitmq findByName(final String name) {
        for (Rabbitmq rabbitmq : integrations.get("env1").getRabbitmqIntegration().getRabbitmq()) {
            if (rabbitmq.getAlias().equals(name)) {
                return rabbitmq;
            }
        }
        throw new DefaultFrameworkException(format("Instance of RabbitMQ by name \"%s\" not found", name));
    }
}
