package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.kafka.KafkaOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import com.knubisoft.testlum.testing.model.global_config.KafkaIntegration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AliasKafkaAdapterTest {

    @Test
    void getStorageNameReturnsKafka() throws Exception {
        final KafkaOperation kafkaOperation = mock(KafkaOperation.class);
        final Integrations integrations = createIntegrationsWithKafka("testAlias", true);
        final AliasKafkaAdapter adapter = new AliasKafkaAdapter(kafkaOperation, integrations);

        final Method method = AliasKafkaAdapter.class.getDeclaredMethod("getStorageName");
        method.setAccessible(true);
        final String result = (String) method.invoke(adapter);

        assertEquals("Kafka", result);
    }

    @Test
    void getIntegrationListReturnsKafkaList() throws Exception {
        final KafkaOperation kafkaOperation = mock(KafkaOperation.class);
        final Integrations integrations = createIntegrationsWithKafka("alias1", true);
        final AliasKafkaAdapter adapter = new AliasKafkaAdapter(kafkaOperation, integrations);

        final Method method = AliasKafkaAdapter.class.getDeclaredMethod("getIntegrationList", Integrations.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        final List<Kafka> result = (List<Kafka>) method.invoke(adapter, integrations);

        assertEquals(1, result.size());
        assertEquals("alias1", result.get(0).getAlias());
    }

    @Test
    void applyAddsEnabledIntegrationToAliasMap() {
        final KafkaOperation kafkaOperation = mock(KafkaOperation.class);
        final Integrations integrations = createIntegrationsWithKafka("myAlias", true);
        final AliasKafkaAdapter adapter = new AliasKafkaAdapter(kafkaOperation, integrations);
        final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

        adapter.apply(aliasMap);

        assertTrue(aliasMap.containsKey("Kafka_myAlias"));
        assertEquals(kafkaOperation, aliasMap.get("Kafka_myAlias"));
    }

    @Test
    void applySkipsDisabledIntegration() {
        final KafkaOperation kafkaOperation = mock(KafkaOperation.class);
        final Integrations integrations = createIntegrationsWithKafka("disabledAlias", false);
        final AliasKafkaAdapter adapter = new AliasKafkaAdapter(kafkaOperation, integrations);
        final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

        adapter.apply(aliasMap);

        assertTrue(aliasMap.isEmpty());
    }

    @Test
    void applyAddsOnlyEnabledIntegrationsFromMixed() {
        final Kafka enabled = new Kafka();
        enabled.setAlias("enabled1");
        enabled.setEnabled(true);
        final Kafka disabled = new Kafka();
        disabled.setAlias("disabled1");
        disabled.setEnabled(false);
        final KafkaIntegration kafkaIntegration = new KafkaIntegration();
        kafkaIntegration.getKafka().add(enabled);
        kafkaIntegration.getKafka().add(disabled);
        final Integrations integrations = new Integrations();
        integrations.setKafkaIntegration(kafkaIntegration);
        final KafkaOperation kafkaOperation = mock(KafkaOperation.class);
        final AliasKafkaAdapter adapter = new AliasKafkaAdapter(kafkaOperation, integrations);
        final Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();

        adapter.apply(aliasMap);

        assertEquals(1, aliasMap.size());
        assertTrue(aliasMap.containsKey("Kafka_enabled1"));
        assertFalse(aliasMap.containsKey("Kafka_disabled1"));
    }

    private Integrations createIntegrationsWithKafka(final String alias, final boolean enabled) {
        final Kafka kafka = new Kafka();
        kafka.setAlias(alias);
        kafka.setEnabled(enabled);
        final KafkaIntegration kafkaIntegration = new KafkaIntegration();
        kafkaIntegration.getKafka().add(kafka);
        final Integrations integrations = new Integrations();
        integrations.setKafkaIntegration(kafkaIntegration);
        return integrations;
    }
}
