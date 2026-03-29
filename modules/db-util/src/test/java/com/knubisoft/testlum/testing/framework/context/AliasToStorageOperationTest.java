package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AliasToStorageOperationTest {

    @Nested
    class GetByNameOrThrow {
        @Test
        void returnsOperationWhenPresent() {
            AbstractStorageOperation operation = mock(AbstractStorageOperation.class);
            Map<String, AbstractStorageOperation> aliasMap = Map.of("db1", operation);
            AliasToStorageOperation impl = createImpl(aliasMap);
            assertEquals(operation, impl.getByNameOrThrow("db1"));
        }

        @Test
        void throwsWhenNameNotFound() {
            AliasToStorageOperation impl = createImpl(Map.of());
            assertThrows(IllegalArgumentException.class, () -> impl.getByNameOrThrow("missing"));
        }
    }

    @Nested
    class GetAlias {
        @Test
        void returnsEmptyMapWhenNoAliases() {
            AliasToStorageOperation impl = createImpl(Map.of());
            assertTrue(impl.getAlias().isEmpty());
        }

        @Test
        void returnsAllRegisteredAliases() {
            AbstractStorageOperation op1 = mock(AbstractStorageOperation.class);
            AbstractStorageOperation op2 = mock(AbstractStorageOperation.class);
            Map<String, AbstractStorageOperation> aliasMap = Map.of("db1", op1, "db2", op2);
            AliasToStorageOperation impl = createImpl(aliasMap);
            assertEquals(2, impl.getAlias().size());
            assertNotNull(impl.getAlias().get("db1"));
            assertNotNull(impl.getAlias().get("db2"));
        }
    }

    private AliasToStorageOperation createImpl(final Map<String, AbstractStorageOperation> map) {
        return new AliasToStorageOperation() {
            @Override
            public AbstractStorageOperation getByNameOrThrow(final String name) {
                if (!map.containsKey(name)) {
                    throw new IllegalArgumentException("Not found: " + name);
                }
                return map.get(name);
            }

            @Override
            public Map<String, AbstractStorageOperation> getAlias() {
                return map;
            }
        };
    }
}
