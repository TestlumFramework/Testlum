package com.testlum.testing.framework.util;

import com.testlum.testing.framework.vault.VaultService;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.AuthStrategies;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;
import com.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    class InjectGoogleConfiguration {
        @Test
        void resolvesEnvironmentVariablesAndPreservesGoogleStrategyType() {
            String path = System.getenv("PATH");
            assertNotNull(path);
            Integrations source = googleConfig("${PATH}", "${PATH}");
            InjectionService service = new InjectionService(new JacksonService(), new SystemVariableService());

            Integrations resolved = service.injectFromSystem(source);

            GoogleAuthStrategy google = assertInstanceOf(GoogleAuthStrategy.class,
                    resolved.getAuthStrategies().getJwtOrBasicOrCustom().get(0));
            assertEquals("GOOGLE", google.getAlias());
            assertEquals(path, google.getClientId());
            assertEquals(path, google.getClientSecret());
            GoogleAuthStrategy original = (GoogleAuthStrategy) source.getAuthStrategies()
                    .getJwtOrBasicOrCustom().get(0);
            assertEquals("${PATH}", original.getClientSecret());
        }

        @Test
        void missingEnvironmentVariableFailsBeforeAuthorization() {
            Integrations source = googleConfig("client", "${THIS_VAR_SHOULD_NOT_EXIST_12345}");
            InjectionService service = new InjectionService(new JacksonService(), new SystemVariableService());
            assertThrows(DefaultFrameworkException.class, () -> service.injectFromSystem(source));
        }

        private Integrations googleConfig(final String clientId, final String clientSecret) {
            GoogleAuthStrategy google = new GoogleAuthStrategy();
            google.setAlias("GOOGLE");
            google.setClientId(clientId);
            google.setClientSecret(clientSecret);
            AuthStrategies strategies = new AuthStrategies();
            strategies.getJwtOrBasicOrCustom().add(google);
            Integrations integrations = new Integrations();
            integrations.setAuthStrategies(strategies);
            return integrations;
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
