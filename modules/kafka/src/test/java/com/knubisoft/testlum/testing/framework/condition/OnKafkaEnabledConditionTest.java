package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Kafka;
import com.knubisoft.testlum.testing.model.global_config.KafkaIntegration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnKafkaEnabledConditionTest {

    private final OnKafkaEnabledCondition condition = new OnKafkaEnabledCondition();

    @Test
    void getIntegrationsReturnsPresentWhenKafkaIntegrationExists() {
        final Kafka kafka = new Kafka();
        kafka.setEnabled(true);
        kafka.setAlias("kafka1");
        final KafkaIntegration kafkaIntegration = new KafkaIntegration();
        kafkaIntegration.getKafka().add(kafka);
        final Integrations integrations = new Integrations();
        integrations.setKafkaIntegration(kafkaIntegration);

        final Optional<List<? extends Integration>> result =
                condition.getIntegrations(Optional.of(integrations));

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("kafka1", result.get().get(0).getAlias());
    }

    @Test
    void getIntegrationsReturnsEmptyWhenIntegrationsIsEmpty() {
        final Optional<List<? extends Integration>> result =
                condition.getIntegrations(Optional.empty());

        assertFalse(result.isPresent());
    }

    @Test
    void getIntegrationsReturnsEmptyWhenKafkaIntegrationIsNull() {
        final Integrations integrations = new Integrations();

        final Optional<List<? extends Integration>> result =
                condition.getIntegrations(Optional.of(integrations));

        assertFalse(result.isPresent());
    }

    @Test
    void getIntegrationsReturnsEmptyListWhenNoKafkaEntries() {
        final KafkaIntegration kafkaIntegration = new KafkaIntegration();
        final Integrations integrations = new Integrations();
        integrations.setKafkaIntegration(kafkaIntegration);

        final Optional<List<? extends Integration>> result =
                condition.getIntegrations(Optional.of(integrations));

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void getIntegrationsReturnsMultipleKafkaEntries() {
        final Kafka kafka1 = new Kafka();
        kafka1.setEnabled(true);
        kafka1.setAlias("kafka1");
        final Kafka kafka2 = new Kafka();
        kafka2.setEnabled(false);
        kafka2.setAlias("kafka2");
        final KafkaIntegration kafkaIntegration = new KafkaIntegration();
        kafkaIntegration.getKafka().add(kafka1);
        kafkaIntegration.getKafka().add(kafka2);
        final Integrations integrations = new Integrations();
        integrations.setKafkaIntegration(kafkaIntegration);

        final Optional<List<? extends Integration>> result =
                condition.getIntegrations(Optional.of(integrations));

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }
}
