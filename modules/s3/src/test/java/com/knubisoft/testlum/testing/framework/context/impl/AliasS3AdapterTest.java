package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.s3.S3Operation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.S3;
import com.knubisoft.testlum.testing.model.global_config.S3Integration;
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

class AliasS3AdapterTest {

    @Nested
    class GetStorageName {

        @Test
        void returnsS3() throws Exception {
            final S3Operation operation = mock(S3Operation.class);
            final Integrations integrations = createIntegrations("alias1", true);
            final AliasS3Adapter adapter = new AliasS3Adapter(operation, integrations);

            final Method method = AliasS3Adapter.class.getDeclaredMethod("getStorageName");
            method.setAccessible(true);
            final String result = (String) method.invoke(adapter);

            assertEquals("S3", result);
        }
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsS3List() throws Exception {
            final S3Operation operation = mock(S3Operation.class);
            final Integrations integrations = createIntegrations("s3-alias", true);
            final AliasS3Adapter adapter = new AliasS3Adapter(operation, integrations);

            final Method method = AliasS3Adapter.class
                    .getDeclaredMethod("getIntegrationList", Integrations.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            final List<S3> result = (List<S3>) method.invoke(adapter, integrations);

            assertEquals(1, result.size());
            assertEquals("s3-alias", result.get(0).getAlias());
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            final S3Operation operation = mock(S3Operation.class);
            final Integrations integrations = createIntegrations("myBucket", true);
            final AliasS3Adapter adapter = new AliasS3Adapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.containsKey("S3_myBucket"));
            assertSame(operation, aliasMap.get("S3_myBucket"));
        }

        @Test
        void skipsDisabledIntegration() {
            final S3Operation operation = mock(S3Operation.class);
            final Integrations integrations = createIntegrations("disabledBucket", false);
            final AliasS3Adapter adapter = new AliasS3Adapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsOnlyEnabledFromMixedList() {
            final S3 enabled = new S3();
            enabled.setAlias("enabled-s3");
            enabled.setEnabled(true);
            final S3 disabled = new S3();
            disabled.setAlias("disabled-s3");
            disabled.setEnabled(false);
            final S3Integration s3Integration = new S3Integration();
            s3Integration.getS3().add(enabled);
            s3Integration.getS3().add(disabled);
            final Integrations integrations = new Integrations();
            integrations.setS3Integration(s3Integration);
            final S3Operation operation = mock(S3Operation.class);
            final AliasS3Adapter adapter = new AliasS3Adapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
            assertTrue(aliasMap.containsKey("S3_enabled-s3"));
            assertFalse(aliasMap.containsKey("S3_disabled-s3"));
        }

        @Test
        void emptyListLeavesMapEmpty() {
            final S3Integration s3Integration = new S3Integration();
            final Integrations integrations = new Integrations();
            integrations.setS3Integration(s3Integration);
            final S3Operation operation = mock(S3Operation.class);
            final AliasS3Adapter adapter = new AliasS3Adapter(operation, integrations);
            final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }

    private Integrations createIntegrations(final String alias, final boolean enabled) {
        final S3 s3 = new S3();
        s3.setAlias(alias);
        s3.setEnabled(enabled);
        final S3Integration s3Integration = new S3Integration();
        s3Integration.getS3().add(s3);
        final Integrations integrations = new Integrations();
        integrations.setS3Integration(s3Integration);
        return integrations;
    }
}
