package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.rabbitmq.RabbitMQOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.knubisoft.testlum.testing.model.global_config.RabbitmqIntegration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AliasRabbitAdapterTest {

    @Nested
    class GetStorageName {

        @Test
        void returnsRabbitmq() throws Exception {
            final RabbitMQOperation operation = mock(RabbitMQOperation.class);
            final Integrations integrations = createIntegrations("alias1", true);
            final AliasRabbitAdapter adapter = new AliasRabbitAdapter(operation, integrations);

            final Method method = AliasRabbitAdapter.class.getDeclaredMethod("getStorageName");
            method.setAccessible(true);
            final String result = (String) method.invoke(adapter);

            assertEquals("Rabbitmq", result);
        }
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsRabbitmqList() throws Exception {
            final RabbitMQOperation operation = mock(RabbitMQOperation.class);
            final Integrations integrations = createIntegrations("rmq1", true);
            final AliasRabbitAdapter adapter = new AliasRabbitAdapter(operation, integrations);

            final Method method = AliasRabbitAdapter.class
                    .getDeclaredMethod("getIntegrationList", Integrations.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            final List<Rabbitmq> result = (List<Rabbitmq>) method.invoke(adapter, integrations);

            assertEquals(1, result.size());
            assertEquals("rmq1", result.get(0).getAlias());
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            final RabbitMQOperation operation = mock(RabbitMQOperation.class);
            final Integrations integrations = createIntegrations("myAlias", true);
            final AliasRabbitAdapter adapter = new AliasRabbitAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.containsKey("Rabbitmq_myAlias"));
            assertSame(operation, aliasMap.get("Rabbitmq_myAlias"));
        }

        @Test
        void skipsDisabledIntegration() {
            final RabbitMQOperation operation = mock(RabbitMQOperation.class);
            final Integrations integrations = createIntegrations("disabledAlias", false);
            final AliasRabbitAdapter adapter = new AliasRabbitAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsOnlyEnabledFromMixedList() {
            final Rabbitmq enabled = new Rabbitmq();
            enabled.setAlias("enabled1");
            enabled.setEnabled(true);
            final Rabbitmq disabled = new Rabbitmq();
            disabled.setAlias("disabled1");
            disabled.setEnabled(false);
            final RabbitmqIntegration rmqIntegration = new RabbitmqIntegration();
            rmqIntegration.getRabbitmq().add(enabled);
            rmqIntegration.getRabbitmq().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setRabbitmqIntegration(rmqIntegration);
            final RabbitMQOperation operation = mock(RabbitMQOperation.class);
            final AliasRabbitAdapter adapter = new AliasRabbitAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("Rabbitmq_enabled1"));
            assertFalse(aliasMap.containsKey("Rabbitmq_disabled1"));
        }

        @Test
        void emptyListLeavesMapEmpty() {
            final RabbitmqIntegration rmqIntegration = new RabbitmqIntegration();
            final Integrations integrations = new Integrations();
            integrations.setRabbitmqIntegration(rmqIntegration);
            final RabbitMQOperation operation = mock(RabbitMQOperation.class);
            final AliasRabbitAdapter adapter = new AliasRabbitAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    private Integrations createIntegrations(final String alias, final boolean enabled) {
        final Rabbitmq rabbitmq = new Rabbitmq();
        rabbitmq.setAlias(alias);
        rabbitmq.setEnabled(enabled);
        final RabbitmqIntegration rmqIntegration = new RabbitmqIntegration();
        rmqIntegration.getRabbitmq().add(rabbitmq);
        final Integrations integrations = new Integrations();
        integrations.setRabbitmqIntegration(rmqIntegration);
        return integrations;
    }
}
