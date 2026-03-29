package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class AliasToStorageOperationServiceTest {

    @Test
    void initPopulatesAliasMapFromAdapters() {
        AbstractStorageOperation operation = mock(AbstractStorageOperation.class);
        AliasAdapter adapter = mock(AliasAdapter.class);
        doAnswer(invocation -> {
            Map<String, AbstractStorageOperation> map = invocation.getArgument(0);
            map.put("POSTGRES", operation);
            return null;
        }).when(adapter).apply(any());

        AliasToStorageOperationService service = new AliasToStorageOperationService(List.of(adapter));
        service.init();

        assertNotNull(service.getAlias());
        assertEquals(1, service.getAlias().size());
        assertEquals(operation, service.getAlias().get("POSTGRES"));
    }

    @Test
    void getByNameOrThrowReturnsOperationForExistingKeyCaseInsensitive() {
        AbstractStorageOperation operation = mock(AbstractStorageOperation.class);
        AliasAdapter adapter = mock(AliasAdapter.class);
        doAnswer(invocation -> {
            Map<String, AbstractStorageOperation> map = invocation.getArgument(0);
            map.put("postgres", operation);
            return null;
        }).when(adapter).apply(any());

        AliasToStorageOperationService service = new AliasToStorageOperationService(List.of(adapter));
        service.init();

        AbstractStorageOperation result = service.getByNameOrThrow("POSTGRES");

        assertEquals(operation, result);
    }

    @Test
    void getByNameOrThrowThrowsExceptionForMissingKey() {
        AliasAdapter adapter = mock(AliasAdapter.class);
        doAnswer(invocation -> {
            Map<String, AbstractStorageOperation> map = invocation.getArgument(0);
            map.put("POSTGRES", mock(AbstractStorageOperation.class));
            return null;
        }).when(adapter).apply(any());

        AliasToStorageOperationService service = new AliasToStorageOperationService(List.of(adapter));
        service.init();

        assertThrows(DefaultFrameworkException.class, () -> service.getByNameOrThrow("REDIS"));
    }

    @Test
    void initWithEmptyAdapterListCreatesEmptyMap() {
        AliasToStorageOperationService service = new AliasToStorageOperationService(Collections.emptyList());
        service.init();

        assertNotNull(service.getAlias());
        assertTrue(service.getAlias().isEmpty());
    }

    @Test
    void initWithMultipleAdaptersPopulatesAllEntries() {
        AbstractStorageOperation op1 = mock(AbstractStorageOperation.class);
        AbstractStorageOperation op2 = mock(AbstractStorageOperation.class);

        AliasAdapter adapter1 = mock(AliasAdapter.class);
        doAnswer(invocation -> {
            Map<String, AbstractStorageOperation> map = invocation.getArgument(0);
            map.put("POSTGRES", op1);
            return null;
        }).when(adapter1).apply(any());

        AliasAdapter adapter2 = mock(AliasAdapter.class);
        doAnswer(invocation -> {
            Map<String, AbstractStorageOperation> map = invocation.getArgument(0);
            map.put("MONGO", op2);
            return null;
        }).when(adapter2).apply(any());

        AliasToStorageOperationService service = new AliasToStorageOperationService(List.of(adapter1, adapter2));
        service.init();

        assertEquals(2, service.getAlias().size());
        assertEquals(op1, service.getAlias().get("POSTGRES"));
        assertEquals(op2, service.getAlias().get("MONGO"));
    }
}
