package com.knubisoft.testlum.testing.framework.configuration.rabbitmq;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COLON;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnRabbitMQEnabledCondition.class})
@RequiredArgsConstructor
public class RabbitMQConfiguration {

    private static final String SCHEMA = "http://";
    private static final String API_PATH = "/api";
    private final ConnectionTemplate connectionTemplate;
    private final Map<String, List<Rabbitmq>> rabbitmqMap = GlobalTestConfigurationProvider.getIntegrations()
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().getRabbitmqIntegration().getRabbitmq()));

    @Bean("rabbitMqClient")
    public Map<AliasEnv, Client> rabbitMqClient() {
        final Map<AliasEnv, Client> clientMap = new HashMap<>();
        rabbitmqMap.forEach((env, rabbitmqList) -> addClientParameters(rabbitmqList, env, clientMap));
        return clientMap;
    }

    private void addClientParameters(final List<Rabbitmq> rabbitmqs,
                                     final String env,
                                     final Map<AliasEnv, Client> clientMap) {
        for (Rabbitmq rabbitmq : rabbitmqs) {
            if (rabbitmq.isEnabled()) {
                Client adminClient = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "RabbitMQ-Admin", rabbitmq.getAlias()),
                        () -> {
                            try {
                                return new Client(createClientParameters(rabbitmq));
                            } catch (URISyntaxException | MalformedURLException e) {
                                throw new DefaultFrameworkException(e.getMessage());
                            }
                        },
                        HealthCheckFactory.forRabbitMqAdmin()
                );
                clientMap.put(new AliasEnv(rabbitmq.getAlias(), env), adminClient);
            }
        }
    }

    private ClientParameters createClientParameters(final Rabbitmq rabbitmq) throws MalformedURLException {
        final String url = SCHEMA + rabbitmq.getHost() + COLON + rabbitmq.getApiPort() + API_PATH;
        return new ClientParameters()
                .url(url)
                .username(rabbitmq.getUsername())
                .password(rabbitmq.getPassword());
    }

    @Bean
    public Map<AliasEnv, AmqpAdmin> amqpAdmin(final Map<AliasEnv, ConnectionFactory> connectionFactory) {
        Map<AliasEnv, AmqpAdmin> adminMap = new HashMap<>();
        connectionFactory.forEach((aliasEnv, factory) -> adminMap.put(aliasEnv, new RabbitAdmin(factory)));
        return adminMap;
    }

    @Bean
    public Map<AliasEnv, RabbitTemplate> rabbitTemplate(final Map<AliasEnv, ConnectionFactory> connectionFactory) {
        Map<AliasEnv, RabbitTemplate> templateMap = new HashMap<>();
        connectionFactory.forEach((aliasEnv, factory) -> templateMap.put(aliasEnv, new RabbitTemplate(factory)));
        return templateMap;
    }

    @Bean
    public Map<AliasEnv, ConnectionFactory> connectionFactory() {
        Map<AliasEnv, ConnectionFactory> connectionFactoryMap = new HashMap<>();
        rabbitmqMap.forEach((env, rabbitmqList) -> addConnectionFactory(rabbitmqList, env, connectionFactoryMap));
        return connectionFactoryMap;
    }

    private void addConnectionFactory(final List<Rabbitmq> rabbitmqList,
                                      final String env,
                                      final Map<AliasEnv, ConnectionFactory> connectionFactoryMap) {
        for (Rabbitmq rabbitmq : rabbitmqList) {
            if (rabbitmq.isEnabled()) {
                CachingConnectionFactory connectionFactory = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "RabbitMQ-AMQP", rabbitmq.getAlias()),
                        () -> createConnectionFactory(rabbitmq),
                        HealthCheckFactory.forRabbitMqAmqp()
                );
                connectionFactoryMap.put(new AliasEnv(rabbitmq.getAlias(), env), connectionFactory);
            }
        }
    }

    private CachingConnectionFactory createConnectionFactory(final Rabbitmq rabbitmq) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitmq.getHost());
        connectionFactory.setPort(rabbitmq.getPort());
        connectionFactory.setUsername(rabbitmq.getUsername());
        connectionFactory.setPassword(rabbitmq.getPassword());
        connectionFactory.setVirtualHost(rabbitmq.getVirtualHost());
        return connectionFactory;
    }
}