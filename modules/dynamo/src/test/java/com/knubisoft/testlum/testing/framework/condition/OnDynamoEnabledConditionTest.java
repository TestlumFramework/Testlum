package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnDynamoEnabledConditionTest {

    private final OnDynamoEnabledCondition condition = new OnDynamoEnabledCondition();

    @Nested
    class GetIntegrations {

        @Test
        void returnsPresentWhenDynamoIntegrationExists() {
            final Dynamo dynamo = new Dynamo();
            dynamo.setEnabled(true);
            dynamo.setAlias("dynamo1");
            final DynamoIntegration dynamoIntegration = new DynamoIntegration();
            dynamoIntegration.getDynamo().add(dynamo);
            final Integrations integrations = new Integrations();
            integrations.setDynamoIntegration(dynamoIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals("dynamo1", result.get().get(0).getAlias());
        }

        @Test
        void returnsEmptyWhenIntegrationsIsEmpty() {
            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.empty());

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyWhenDynamoIntegrationIsNull() {
            final Integrations integrations = new Integrations();

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertFalse(result.isPresent());
        }

        @Test
        void returnsEmptyListWhenNoDynamoEntries() {
            final DynamoIntegration dynamoIntegration = new DynamoIntegration();
            final Integrations integrations = new Integrations();
            integrations.setDynamoIntegration(dynamoIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        void returnsMultipleDynamoEntries() {
            final Dynamo d1 = new Dynamo();
            d1.setEnabled(true);
            d1.setAlias("dynamo-primary");
            final Dynamo d2 = new Dynamo();
            d2.setEnabled(false);
            d2.setAlias("dynamo-secondary");
            final DynamoIntegration dynamoIntegration = new DynamoIntegration();
            dynamoIntegration.getDynamo().add(d1);
            dynamoIntegration.getDynamo().add(d2);
            final Integrations integrations = new Integrations();
            integrations.setDynamoIntegration(dynamoIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
        }

        @Test
        void preservesOrderOfEntries() {
            final Dynamo d1 = new Dynamo();
            d1.setAlias("first");
            d1.setEnabled(true);
            final Dynamo d2 = new Dynamo();
            d2.setAlias("second");
            d2.setEnabled(false);
            final Dynamo d3 = new Dynamo();
            d3.setAlias("third");
            d3.setEnabled(true);
            final DynamoIntegration dynamoIntegration = new DynamoIntegration();
            dynamoIntegration.getDynamo().add(d1);
            dynamoIntegration.getDynamo().add(d2);
            dynamoIntegration.getDynamo().add(d3);
            final Integrations integrations = new Integrations();
            integrations.setDynamoIntegration(dynamoIntegration);

            final Optional<List<? extends Integration>> result =
                    condition.getIntegrations(Optional.of(integrations));

            assertTrue(result.isPresent());
            assertEquals(3, result.get().size());
            assertEquals("first", result.get().get(0).getAlias());
            assertEquals("second", result.get().get(1).getAlias());
            assertEquals("third", result.get().get(2).getAlias());
        }
    }
}
