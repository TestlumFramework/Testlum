package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AliasAbstractAdapterTest {

    @Mock
    private AbstractStorageOperation operation;

    private TestAliasAdapter createAdapter(final List<TestIntegration> integrationList) {
        Integrations integrations = mock(Integrations.class);
        return new TestAliasAdapter(operation, integrations, integrationList, "POSTGRES");
    }

    @Nested
    class ApplyWithEnabledIntegrations {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            TestIntegration integration = new TestIntegration("myAlias", true);
            TestAliasAdapter adapter = createAdapter(List.of(integration));

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("POSTGRES_myAlias"));
            assertSame(operation, aliasMap.get("POSTGRES_myAlias"));
        }

        @Test
        void addsMultipleEnabledIntegrations() {
            List<TestIntegration> integrations = List.of(
                    new TestIntegration("alias1", true),
                    new TestIntegration("alias2", true),
                    new TestIntegration("alias3", true)
            );
            TestAliasAdapter adapter = createAdapter(integrations);

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(3, aliasMap.size());
            assertTrue(aliasMap.containsKey("POSTGRES_alias1"));
            assertTrue(aliasMap.containsKey("POSTGRES_alias2"));
            assertTrue(aliasMap.containsKey("POSTGRES_alias3"));
        }
    }

    @Nested
    class ApplyWithDisabledIntegrations {

        @Test
        void skipsDisabledIntegration() {
            TestIntegration integration = new TestIntegration("disabled", false);
            TestAliasAdapter adapter = createAdapter(List.of(integration));

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    @Nested
    class ApplyWithEmptyList {

        @Test
        void emptyIntegrationListLeavesMapUnchanged() {
            TestAliasAdapter adapter = createAdapter(new ArrayList<>());

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    @Nested
    class ApplyWithMixedIntegrations {

        @Test
        void onlyAddsEnabledIntegrationsFromMixedList() {
            List<TestIntegration> integrations = List.of(
                    new TestIntegration("enabled1", true),
                    new TestIntegration("disabled1", false),
                    new TestIntegration("enabled2", true),
                    new TestIntegration("disabled2", false)
            );
            TestAliasAdapter adapter = createAdapter(integrations);

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(2, aliasMap.size());
            assertTrue(aliasMap.containsKey("POSTGRES_enabled1"));
            assertTrue(aliasMap.containsKey("POSTGRES_enabled2"));
            assertFalse(aliasMap.containsKey("POSTGRES_disabled1"));
            assertFalse(aliasMap.containsKey("POSTGRES_disabled2"));
        }

        @Test
        void keyFormatIsStorageNameUnderscoreAlias() {
            TestIntegration integration = new TestIntegration("db1", true);
            Integrations integrations = mock(Integrations.class);
            TestAliasAdapter adapter = new TestAliasAdapter(operation, integrations,
                    List.of(integration), "MYSQL");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.containsKey("MYSQL_db1"));
        }
    }

    private static class TestIntegration extends Integration {
        TestIntegration(final String alias, final boolean enabled) {
            this.alias = alias;
            this.enabled = enabled;
        }
    }

    private static class TestAliasAdapter extends AbstractAliasAdapter {

        private final List<TestIntegration> integrationList;
        private final String storageName;

        TestAliasAdapter(final AbstractStorageOperation operation,
                         final Integrations integrations,
                         final List<TestIntegration> integrationList,
                         final String storageName) {
            super(operation, integrations);
            this.integrationList = integrationList;
            this.storageName = storageName;
        }

        @Override
        protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
            return integrationList;
        }

        @Override
        protected String getStorageName() {
            return storageName;
        }
    }
}
