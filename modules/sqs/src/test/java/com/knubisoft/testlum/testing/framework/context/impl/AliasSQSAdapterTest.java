package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sqs.SQSOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import com.knubisoft.testlum.testing.model.global_config.SqsIntegration;
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

class AliasSQSAdapterTest {

    @Nested
    class GetStorageName {

        @Test
        void returnsSQS() throws Exception {
            final SQSOperation operation = mock(SQSOperation.class);
            final Integrations integrations = createIntegrations("alias1", true);
            final AliasSQSAdapter adapter = new AliasSQSAdapter(operation, integrations);

            final Method method = AliasSQSAdapter.class.getDeclaredMethod("getStorageName");
            method.setAccessible(true);
            final String result = (String) method.invoke(adapter);

            assertEquals("SQS", result);
        }
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsSqsList() throws Exception {
            final SQSOperation operation = mock(SQSOperation.class);
            final Integrations integrations = createIntegrations("sqs-alias", true);
            final AliasSQSAdapter adapter = new AliasSQSAdapter(operation, integrations);

            final Method method = AliasSQSAdapter.class
                    .getDeclaredMethod("getIntegrationList", Integrations.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            final List<Sqs> result = (List<Sqs>) method.invoke(adapter, integrations);

            assertEquals(1, result.size());
            assertEquals("sqs-alias", result.get(0).getAlias());
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            final SQSOperation operation = mock(SQSOperation.class);
            final Integrations integrations = createIntegrations("myQueue", true);
            final AliasSQSAdapter adapter = new AliasSQSAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.containsKey("SQS_myQueue"));
            assertSame(operation, aliasMap.get("SQS_myQueue"));
        }

        @Test
        void skipsDisabledIntegration() {
            final SQSOperation operation = mock(SQSOperation.class);
            final Integrations integrations = createIntegrations("disabledQueue", false);
            final AliasSQSAdapter adapter = new AliasSQSAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsOnlyEnabledFromMixedList() {
            final Sqs enabled = new Sqs();
            enabled.setAlias("enabled-sqs");
            enabled.setEnabled(true);
            final Sqs disabled = new Sqs();
            disabled.setAlias("disabled-sqs");
            disabled.setEnabled(false);
            final SqsIntegration sqsIntegration = new SqsIntegration();
            sqsIntegration.getSqs().add(enabled);
            sqsIntegration.getSqs().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setSqsIntegration(sqsIntegration);
            final SQSOperation operation = mock(SQSOperation.class);
            final AliasSQSAdapter adapter = new AliasSQSAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("SQS_enabled-sqs"));
            assertFalse(aliasMap.containsKey("SQS_disabled-sqs"));
        }

        @Test
        void emptyListLeavesMapEmpty() {
            final SqsIntegration sqsIntegration = new SqsIntegration();
            final Integrations integrations = new Integrations();
            integrations.setSqsIntegration(sqsIntegration);
            final SQSOperation operation = mock(SQSOperation.class);
            final AliasSQSAdapter adapter = new AliasSQSAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    private Integrations createIntegrations(final String alias, final boolean enabled) {
        final Sqs sqs = new Sqs();
        sqs.setAlias(alias);
        sqs.setEnabled(enabled);
        final SqsIntegration sqsIntegration = new SqsIntegration();
        sqsIntegration.getSqs().add(sqs);
        final Integrations integrations = new Integrations();
        integrations.setSqsIntegration(sqsIntegration);
        return integrations;
    }
}
