package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AliasAdapterTest {

    @Test
    void applyPopulatesAliasMap() {
        Map<String, AbstractStorageOperation> result = new HashMap<>();
        AbstractStorageOperation operation = mock(AbstractStorageOperation.class);

        AliasAdapter adapter = aliasMap -> aliasMap.put("testAlias", operation);
        adapter.apply(result);

        assertEquals(1, result.size());
        assertEquals(operation, result.get("testAlias"));
    }

    @Test
    void applyWithEmptyMapDoesNothing() {
        Map<String, AbstractStorageOperation> result = new HashMap<>();
        AliasAdapter adapter = aliasMap -> { };
        adapter.apply(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void applyWithMultipleEntries() {
        Map<String, AbstractStorageOperation> result = new HashMap<>();
        AbstractStorageOperation op1 = mock(AbstractStorageOperation.class);
        AbstractStorageOperation op2 = mock(AbstractStorageOperation.class);

        AliasAdapter adapter = aliasMap -> {
            aliasMap.put("db1", op1);
            aliasMap.put("db2", op2);
        };
        adapter.apply(result);

        assertEquals(2, result.size());
    }
}
