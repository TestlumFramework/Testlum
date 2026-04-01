package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.dynamodb.DynamoDBOperation;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
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

class AliasDynamoAdapterTest {

    @Nested
    class GetStorageName {

        @Test
        void returnsDYNAMO() throws Exception {
            final DynamoDBOperation operation = mock(DynamoDBOperation.class);
            final Integrations integrations = createIntegrations("alias1", true);
            final AliasDynamoAdapter adapter = new AliasDynamoAdapter(operation, integrations);

            final Method method = AliasDynamoAdapter.class.getDeclaredMethod("getStorageName");
            method.setAccessible(true);
            final String result = (String) method.invoke(adapter);

            assertEquals("DYNAMO", result);
        }
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsDynamoList() throws Exception {
            final DynamoDBOperation operation = mock(DynamoDBOperation.class);
            final Integrations integrations = createIntegrations("dynamo-alias", true);
            final AliasDynamoAdapter adapter = new AliasDynamoAdapter(operation, integrations);

            final Method method = AliasDynamoAdapter.class
                    .getDeclaredMethod("getIntegrationList", Integrations.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            final List<Dynamo> result = (List<Dynamo>) method.invoke(adapter, integrations);

            assertEquals(1, result.size());
            assertEquals("dynamo-alias", result.get(0).getAlias());
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            final DynamoDBOperation operation = mock(DynamoDBOperation.class);
            final Integrations integrations = createIntegrations("myTable", true);
            final AliasDynamoAdapter adapter = new AliasDynamoAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.containsKey("DYNAMO_myTable"));
            assertSame(operation, aliasMap.get("DYNAMO_myTable"));
        }

        @Test
        void skipsDisabledIntegration() {
            final DynamoDBOperation operation = mock(DynamoDBOperation.class);
            final Integrations integrations = createIntegrations("disabledTable", false);
            final AliasDynamoAdapter adapter = new AliasDynamoAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsOnlyEnabledFromMixedList() {
            final Dynamo enabled = new Dynamo();
            enabled.setAlias("enabled-dynamo");
            enabled.setEnabled(true);
            final Dynamo disabled = new Dynamo();
            disabled.setAlias("disabled-dynamo");
            disabled.setEnabled(false);
            final DynamoIntegration dynamoIntegration = new DynamoIntegration();
            dynamoIntegration.getDynamo().add(enabled);
            dynamoIntegration.getDynamo().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setDynamoIntegration(dynamoIntegration);
            final DynamoDBOperation operation = mock(DynamoDBOperation.class);
            final AliasDynamoAdapter adapter = new AliasDynamoAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("DYNAMO_enabled-dynamo"));
            assertFalse(aliasMap.containsKey("DYNAMO_disabled-dynamo"));
        }

        @Test
        void emptyListLeavesMapEmpty() {
            final DynamoIntegration dynamoIntegration = new DynamoIntegration();
            final Integrations integrations = new Integrations();
            integrations.setDynamoIntegration(dynamoIntegration);
            final DynamoDBOperation operation = mock(DynamoDBOperation.class);
            final AliasDynamoAdapter adapter = new AliasDynamoAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    private Integrations createIntegrations(final String alias, final boolean enabled) {
        final Dynamo dynamo = new Dynamo();
        dynamo.setAlias(alias);
        dynamo.setEnabled(enabled);
        final DynamoIntegration dynamoIntegration = new DynamoIntegration();
        dynamoIntegration.getDynamo().add(dynamo);
        final Integrations integrations = new Integrations();
        integrations.setDynamoIntegration(dynamoIntegration);
        return integrations;
    }
}
