package com.knubisoft.cott.testing.framework.configuration.rabbitmq;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnRabbitMQEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Rabbitmq;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.HttpComponentsRestTemplateConfigurator;
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

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.COLON;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Configuration
@Conditional({OnRabbitMQEnabledCondition.class})
public class RabbitMQConfiguration {
    private static final String SCHEMA = "http://";
    private static final String API_PATH = "/api";

    private final Map<String, List<Rabbitmq>> rabbitMap = GlobalTestConfigurationProvider.getIntegrations()
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().getRabbitmqIntegration().getRabbitmq()));

    @Bean("rabbitMqClient")
    public Map<String, Client> client() throws MalformedURLException, URISyntaxException {
        final Map<String, Client> clients = new HashMap<>();
        for (Map.Entry<String, List<Rabbitmq>> entry : rabbitMap.entrySet()) {
            addClientParameters(entry.getKey(), entry.getValue(), clients);
        }
        return clients;
    }

    private void addClientParameters(final String envName,
                                     final List<Rabbitmq> rabbitmqs,
                                     final Map<String, Client> clients) throws
            MalformedURLException, URISyntaxException {
        for (Rabbitmq rabbitmq : rabbitmqs) {
            if (rabbitmq.isEnabled()) {
                ClientParameters clientParameters = createClientParameters(rabbitmq);
                clients.put(envName + UNDERSCORE + rabbitmq.getAlias(),
                        new Client(clientParameters));
            }
        }
    }

    private ClientParameters createClientParameters(final Rabbitmq rabbitmq) throws MalformedURLException {
        final String url = SCHEMA + rabbitmq.getHost() + COLON + rabbitmq.getApiPort() + API_PATH;
        return new ClientParameters()
                .url(url)
                .username(rabbitmq.getUsername())
                .password(rabbitmq.getPassword())
                .restTemplateConfigurator(new HttpComponentsRestTemplateConfigurator());
    }

    @Bean
    public Map<String, AmqpAdmin> amqpAdmin(final Map<String, ConnectionFactory> connectionFactory) {
        Map<String, AmqpAdmin> adminMap = new HashMap<>();
        for (Map.Entry<String, ConnectionFactory> entry : connectionFactory.entrySet()) {
            adminMap.put(entry.getKey(), new RabbitAdmin(entry.getValue()));
        }
        return adminMap;
    }

    @Bean
    public Map<String, RabbitTemplate> rabbitTemplate(final Map<String, ConnectionFactory> connectionFactory) {
        Map<String, RabbitTemplate> templateMap = new HashMap<>();
        for (Map.Entry<String, ConnectionFactory> entry : connectionFactory.entrySet()) {
            templateMap.put(entry.getKey(), new RabbitTemplate(entry.getValue()));
        }
        return templateMap;
    }

    @Bean
    public Map<String, ConnectionFactory> connectionFactory() {
        Map<String, ConnectionFactory> connectionFactoryMap = new HashMap<>();
        rabbitMap.forEach((s, rabbitmqs) -> addConnectionFactory(s, rabbitmqs, connectionFactoryMap));
        return connectionFactoryMap;
    }

    private void addConnectionFactory(final String envName,
                                      final List<Rabbitmq> rabbitmqs,
                                      final Map<String, ConnectionFactory> connectionFactoryMap) {
        for (Rabbitmq rabbitmq : rabbitmqs) {
            if (rabbitmq.isEnabled()) {
                CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
                connectionFactoryMap.put(envName + UNDERSCORE + rabbitmq.getAlias(),
                        configureConnectionFactory(rabbitmq, connectionFactory));
            }
        }
    }

    private CachingConnectionFactory configureConnectionFactory(final Rabbitmq rabbitmq,
                                                                final CachingConnectionFactory connectionFactory) {
        connectionFactory.setHost(rabbitmq.getHost());
        connectionFactory.setPort(rabbitmq.getPort());
        connectionFactory.setUsername(rabbitmq.getUsername());
        connectionFactory.setPassword(rabbitmq.getPassword());
        connectionFactory.setVirtualHost(rabbitmq.getVirtualHost());
        return connectionFactory;
    }
}
