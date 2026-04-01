package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.ses.SESOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import com.knubisoft.testlum.testing.model.global_config.SesIntegration;
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

class AliasSESAdapterTest {

    @Nested
    class GetStorageName {

        @Test
        void returnsSES() throws Exception {
            final SESOperation operation = mock(SESOperation.class);
            final Integrations integrations = createIntegrations("alias1", true);
            final AliasSESAdapter adapter = new AliasSESAdapter(operation, integrations);

            final Method method = AliasSESAdapter.class.getDeclaredMethod("getStorageName");
            method.setAccessible(true);
            final String result = (String) method.invoke(adapter);

            assertEquals("SES", result);
        }
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsSesList() throws Exception {
            final SESOperation operation = mock(SESOperation.class);
            final Integrations integrations = createIntegrations("ses-alias", true);
            final AliasSESAdapter adapter = new AliasSESAdapter(operation, integrations);

            final Method method = AliasSESAdapter.class
                    .getDeclaredMethod("getIntegrationList", Integrations.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            final List<Ses> result = (List<Ses>) method.invoke(adapter, integrations);

            assertEquals(1, result.size());
            assertEquals("ses-alias", result.get(0).getAlias());
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            final SESOperation operation = mock(SESOperation.class);
            final Integrations integrations = createIntegrations("myEmail", true);
            final AliasSESAdapter adapter = new AliasSESAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.containsKey("SES_myEmail"));
            assertSame(operation, aliasMap.get("SES_myEmail"));
        }

        @Test
        void skipsDisabledIntegration() {
            final SESOperation operation = mock(SESOperation.class);
            final Integrations integrations = createIntegrations("disabledEmail", false);
            final AliasSESAdapter adapter = new AliasSESAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsOnlyEnabledFromMixedList() {
            final Ses enabled = new Ses();
            enabled.setAlias("enabled-ses");
            enabled.setEnabled(true);
            final Ses disabled = new Ses();
            disabled.setAlias("disabled-ses");
            disabled.setEnabled(false);
            final SesIntegration sesIntegration = new SesIntegration();
            sesIntegration.getSes().add(enabled);
            sesIntegration.getSes().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setSesIntegration(sesIntegration);
            final SESOperation operation = mock(SESOperation.class);
            final AliasSESAdapter adapter = new AliasSESAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("SES_enabled-ses"));
            assertFalse(aliasMap.containsKey("SES_disabled-ses"));
        }

        @Test
        void emptyListLeavesMapEmpty() {
            final SesIntegration sesIntegration = new SesIntegration();
            final Integrations integrations = new Integrations();
            integrations.setSesIntegration(sesIntegration);
            final SESOperation operation = mock(SESOperation.class);
            final AliasSESAdapter adapter = new AliasSESAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    private Integrations createIntegrations(final String alias, final boolean enabled) {
        final Ses ses = new Ses();
        ses.setAlias(alias);
        ses.setEnabled(enabled);
        final SesIntegration sesIntegration = new SesIntegration();
        sesIntegration.getSes().add(ses);
        final Integrations integrations = new Integrations();
        integrations.setSesIntegration(sesIntegration);
        return integrations;
    }
}
