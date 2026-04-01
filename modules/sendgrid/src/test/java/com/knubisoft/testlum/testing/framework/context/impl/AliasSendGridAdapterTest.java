package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sendgrid.SendGridOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import com.knubisoft.testlum.testing.model.global_config.SendgridIntegration;
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

class AliasSendGridAdapterTest {

    @Nested
    class GetStorageName {

        @Test
        void returnsSendgrid() throws Exception {
            final SendGridOperation operation = mock(SendGridOperation.class);
            final Integrations integrations = createIntegrations("alias1", true);
            final AliasSendGridAdapter adapter = new AliasSendGridAdapter(operation, integrations);

            final Method method = AliasSendGridAdapter.class.getDeclaredMethod("getStorageName");
            method.setAccessible(true);
            final String result = (String) method.invoke(adapter);

            assertEquals("Sendgrid", result);
        }
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsSendgridList() throws Exception {
            final SendGridOperation operation = mock(SendGridOperation.class);
            final Integrations integrations = createIntegrations("sg-alias", true);
            final AliasSendGridAdapter adapter = new AliasSendGridAdapter(operation, integrations);

            final Method method = AliasSendGridAdapter.class
                    .getDeclaredMethod("getIntegrationList", Integrations.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            final List<Sendgrid> result = (List<Sendgrid>) method.invoke(adapter, integrations);

            assertEquals(1, result.size());
            assertEquals("sg-alias", result.get(0).getAlias());
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            final SendGridOperation operation = mock(SendGridOperation.class);
            final Integrations integrations = createIntegrations("myGrid", true);
            final AliasSendGridAdapter adapter = new AliasSendGridAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.containsKey("Sendgrid_myGrid"));
            assertSame(operation, aliasMap.get("Sendgrid_myGrid"));
        }

        @Test
        void skipsDisabledIntegration() {
            final SendGridOperation operation = mock(SendGridOperation.class);
            final Integrations integrations = createIntegrations("disabledGrid", false);
            final AliasSendGridAdapter adapter = new AliasSendGridAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsOnlyEnabledFromMixedList() {
            final Sendgrid enabled = new Sendgrid();
            enabled.setAlias("enabled-sg");
            enabled.setEnabled(true);
            final Sendgrid disabled = new Sendgrid();
            disabled.setAlias("disabled-sg");
            disabled.setEnabled(false);
            final SendgridIntegration sendgridIntegration = new SendgridIntegration();
            sendgridIntegration.getSendgrid().add(enabled);
            sendgridIntegration.getSendgrid().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setSendgridIntegration(sendgridIntegration);
            final SendGridOperation operation = mock(SendGridOperation.class);
            final AliasSendGridAdapter adapter = new AliasSendGridAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("Sendgrid_enabled-sg"));
            assertFalse(aliasMap.containsKey("Sendgrid_disabled-sg"));
        }

        @Test
        void emptyListLeavesMapEmpty() {
            final SendgridIntegration sendgridIntegration = new SendgridIntegration();
            final Integrations integrations = new Integrations();
            integrations.setSendgridIntegration(sendgridIntegration);
            final SendGridOperation operation = mock(SendGridOperation.class);
            final AliasSendGridAdapter adapter = new AliasSendGridAdapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    private Integrations createIntegrations(final String alias, final boolean enabled) {
        final Sendgrid sendgrid = new Sendgrid();
        sendgrid.setAlias(alias);
        sendgrid.setEnabled(enabled);
        final SendgridIntegration sendgridIntegration = new SendgridIntegration();
        sendgridIntegration.getSendgrid().add(sendgrid);
        final Integrations integrations = new Integrations();
        integrations.setSendgridIntegration(sendgridIntegration);
        return integrations;
    }
}
