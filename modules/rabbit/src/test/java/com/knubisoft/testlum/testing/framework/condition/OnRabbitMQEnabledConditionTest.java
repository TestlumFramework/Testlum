package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.knubisoft.testlum.testing.model.global_config.RabbitmqIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnRabbitMQEnabledConditionTest {

    private final OnRabbitMQEnabledCondition condition = new OnRabbitMQEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenRabbitmqIntegrationExists() {
            final Rabbitmq rabbitmq = new Rabbitmq();
            rabbitmq.setEnabled(true);
            rabbitmq.setAlias("rmq1");
            final RabbitmqIntegration rabbitmqIntegration = new RabbitmqIntegration();
            rabbitmqIntegration.getRabbitmq().add(rabbitmq);
            final Integrations integrations = new Integrations();
            integrations.setRabbitmqIntegration(rabbitmqIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("rmq1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenRabbitmqIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoRabbitmqEntries() {
            final RabbitmqIntegration rabbitmqIntegration = new RabbitmqIntegration();
            final Integrations integrations = new Integrations();
            integrations.setRabbitmqIntegration(rabbitmqIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleRabbitmqEntries() {
            final Rabbitmq rmq1 = new Rabbitmq();
            rmq1.setEnabled(true);
            rmq1.setAlias("rmq1");
            final Rabbitmq rmq2 = new Rabbitmq();
            rmq2.setEnabled(false);
            rmq2.setAlias("rmq2");
            final RabbitmqIntegration rabbitmqIntegration = new RabbitmqIntegration();
            rabbitmqIntegration.getRabbitmq().add(rmq1);
            rabbitmqIntegration.getRabbitmq().add(rmq2);
            final Integrations integrations = new Integrations();
            integrations.setRabbitmqIntegration(rabbitmqIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void returnsCorrectAliasForEachEntry() {
            final Rabbitmq rmq1 = new Rabbitmq();
            rmq1.setAlias("primary");
            rmq1.setEnabled(true);
            final Rabbitmq rmq2 = new Rabbitmq();
            rmq2.setAlias("secondary");
            rmq2.setEnabled(true);
            final RabbitmqIntegration rabbitmqIntegration = new RabbitmqIntegration();
            rabbitmqIntegration.getRabbitmq().add(rmq1);
            rabbitmqIntegration.getRabbitmq().add(rmq2);
            final Integrations integrations = new Integrations();
            integrations.setRabbitmqIntegration(rabbitmqIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals("primary", result.get().get(0).getAlias());
            assertEquals("secondary", result.get().get(1).getAlias());
        }
    }
}
