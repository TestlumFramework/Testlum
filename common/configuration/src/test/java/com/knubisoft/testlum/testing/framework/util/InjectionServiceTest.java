package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.vault.VaultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class InjectionServiceTest {

    private JacksonService jacksonService;
    private SystemVariableService systemVariableService;
    private InjectionService injectionService;

    @BeforeEach
    void setUp() {
        jacksonService = mock(JacksonService.class);
        systemVariableService = mock(SystemVariableService.class);
        injectionService = new InjectionService(jacksonService, systemVariableService);
    }

    @Nested
    class InjectFromSystem {
        @Test
        void delegatesToSystemVariableService() {
            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"key\":\"val\"}");
            when(systemVariableService.inject(anyString())).thenReturn("{\"key\":\"resolved\"}");
            when(jacksonService.readCopiedValue(anyString(), any(Class.class)))
                    .thenReturn("resolved");

            final String result = injectionService.injectFromSystem("original");
            assertNotNull(result);
            verify(systemVariableService).inject("{\"key\":\"val\"}");
        }
    }

    @Nested
    class InjectFromVault {
        @Test
        void delegatesToVaultService() {
            final VaultService vaultService = mock(VaultService.class);

            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"s\":\"{{v.k}}\"}");
            when(vaultService.inject(anyString())).thenReturn("{\"s\":\"resolved\"}");
            when(jacksonService.readCopiedValue(anyString(), any(Class.class)))
                    .thenReturn("resolved");

            final String result = injectionService.injectFromVault(vaultService, "original");
            assertNotNull(result);
            verify(vaultService).inject("{\"s\":\"{{v.k}}\"}");
        }
    }

    @Nested
    class InjectGeneric {
        @Test
        void appliesCustomInjectorFunction() {
            when(jacksonService.writeValueToCopiedString("hello")).thenReturn("\"hello\"");
            when(jacksonService.readCopiedValue("\"HELLO\"", String.class)).thenReturn("HELLO");

            final String result = injectionService.inject("hello", String::toUpperCase);
            assertEquals("HELLO", result);
        }

        @Test
        void identityFunctionReturnsOriginal() {
            when(jacksonService.writeValueToCopiedString("unchanged")).thenReturn("\"unchanged\"");
            when(jacksonService.readCopiedValue("\"unchanged\"", String.class)).thenReturn("unchanged");

            final String result = injectionService.inject("unchanged", s -> s);
            assertEquals("unchanged", result);
        }
    }
}
