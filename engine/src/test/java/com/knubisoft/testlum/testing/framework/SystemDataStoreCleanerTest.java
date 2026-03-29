package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class SystemDataStoreCleanerTest {

    private SystemDataStoreCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new SystemDataStoreCleaner();
    }

    @Test
    void clearAllCallsClearSystemOnEachOperation() {
        AliasToStorageOperation aliasToStorageOperation = mock(AliasToStorageOperation.class);
        AbstractStorageOperation op1 = mock(AbstractStorageOperation.class);
        AbstractStorageOperation op2 = mock(AbstractStorageOperation.class);

        Map<String, AbstractStorageOperation> metadataMap = new LinkedHashMap<>();
        metadataMap.put("db1", op1);
        metadataMap.put("db2", op2);
        when(aliasToStorageOperation.getAlias()).thenReturn(metadataMap);

        cleaner.clearAll(aliasToStorageOperation);

        verify(op1).clearSystem();
        verify(op2).clearSystem();
    }

    @Test
    void clearAllHandlesIntegrationDisabledExceptionGracefully() {
        AliasToStorageOperation aliasToStorageOperation = mock(AliasToStorageOperation.class);
        AbstractStorageOperation op = mock(AbstractStorageOperation.class);
        doThrow(new IntegrationDisabledException("disabled")).when(op).clearSystem();

        Map<String, AbstractStorageOperation> metadataMap = new LinkedHashMap<>();
        metadataMap.put("db1", op);
        when(aliasToStorageOperation.getAlias()).thenReturn(metadataMap);

        assertDoesNotThrow(() -> cleaner.clearAll(aliasToStorageOperation));
    }

    @Test
    void clearAllWorksWithEmptyMap() {
        AliasToStorageOperation aliasToStorageOperation = mock(AliasToStorageOperation.class);
        when(aliasToStorageOperation.getAlias()).thenReturn(Collections.emptyMap());

        assertDoesNotThrow(() -> cleaner.clearAll(aliasToStorageOperation));
    }

    @Test
    void clearAllCallsClearSystemOnAllEntriesEvenWhenSomeThrow() {
        AbstractStorageOperation op1 = mock(AbstractStorageOperation.class);
        AbstractStorageOperation op2 = mock(AbstractStorageOperation.class);
        AbstractStorageOperation op3 = mock(AbstractStorageOperation.class);
        doThrow(new IntegrationDisabledException("disabled")).when(op1).clearSystem();
        doNothing().when(op2).clearSystem();
        doThrow(new IntegrationDisabledException("disabled")).when(op3).clearSystem();

        Map<String, AbstractStorageOperation> metadataMap = new LinkedHashMap<>();
        metadataMap.put("db1", op1);
        metadataMap.put("db2", op2);
        metadataMap.put("db3", op3);
        AliasToStorageOperation aliasToStorageOperation = mock(AliasToStorageOperation.class);
        when(aliasToStorageOperation.getAlias()).thenReturn(metadataMap);

        cleaner.clearAll(aliasToStorageOperation);

        verify(op1).clearSystem();
        verify(op2).clearSystem();
        verify(op3).clearSystem();
    }
}
