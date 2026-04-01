package com.knubisoft.testlum.testing.framework.db.kafka;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class KafkaOperationTest {

    @Test
    void applyReturnsNull() {
        final Map<AliasEnv, KafkaConsumer<String, String>> consumers = new HashMap<>();
        final Map<AliasEnv, AdminClient> admins = new HashMap<>();
        final KafkaOperation operation = new KafkaOperation(consumers, admins);
        final Source source = mock(Source.class);

        final AbstractStorageOperation.StorageOperationResult result = operation.apply(source, "alias");

        assertNull(result);
    }

    @Test
    void clearSystemDoesNotThrowWhenEmpty() {
        final Map<AliasEnv, KafkaConsumer<String, String>> consumers = new HashMap<>();
        final Map<AliasEnv, AdminClient> admins = new HashMap<>();
        final KafkaOperation operation = new KafkaOperation(consumers, admins);

        operation.clearSystem();
    }
}
