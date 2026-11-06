package com.testlum.testing.framework.vault;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaultServiceTest {

    private VaultTemplate vaultTemplate;
    private VaultService vaultService;

    @BeforeEach
    void setUp() {
        vaultTemplate = mock(VaultTemplate.class);
        vaultService = new VaultService(vaultTemplate);
    }

    @Nested
    class Inject {
        @Test
        void blankInputReturnsAsIs() {
            assertNull(vaultService.inject(null));
            assertEquals("", vaultService.inject(""));
            assertEquals("   ", vaultService.inject("   "));
        }

        @Test
        void noPlaceholdersReturnsUnchanged() {
            assertEquals("plain text", vaultService.inject("plain text"));
        }

        @Test
        void replacesVaultPlaceholder() {
            final VaultResponse response = new VaultResponse();
            response.setData(Map.of("data", Map.of("password", "s3cret")));
            when(vaultTemplate.read("secret/myapp")).thenReturn(response);

            final String result = vaultService.inject("pwd={{secret/myapp.password}}");
            assertEquals("pwd=s3cret", result);
        }

        @Test
        void replacesMultiplePlaceholders() {
            final VaultResponse resp1 = new VaultResponse();
            resp1.setData(Map.of("data", Map.of("user", "admin")));
            when(vaultTemplate.read("secret/db")).thenReturn(resp1);

            final VaultResponse resp2 = new VaultResponse();
            resp2.setData(Map.of("data", Map.of("pass", "pw123")));
            when(vaultTemplate.read("secret/db2")).thenReturn(resp2);

            final String result = vaultService.inject(
                    "u={{secret/db.user}}&p={{secret/db2.pass}}");
            assertEquals("u=admin&p=pw123", result);
        }

        @Test
        void throwsWhenPathNotFound() {
            when(vaultTemplate.read("secret/missing")).thenReturn(null);
            assertThrows(DefaultFrameworkException.class,
                    () -> vaultService.inject("{{secret/missing.key}}"));
        }

        @Test
        void throwsWhenKeyNotFound() {
            final VaultResponse response = new VaultResponse();
            response.setData(Map.of("data", Map.of("other", "value")));
            when(vaultTemplate.read("secret/app")).thenReturn(response);
            assertThrows(DefaultFrameworkException.class,
                    () -> vaultService.inject("{{secret/app.missing}}"));
        }

        @Test
        void throwsWhenVaultUnavailableWithEndpoint() {
            final VaultService serviceWithUri = new VaultService(vaultTemplate, "http://localhost:8200");
            when(vaultTemplate.read("secret/app")).thenThrow(
                    new ResourceAccessException("Connection refused"));

            DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                    () -> serviceWithUri.inject("{{secret/app.key}}"));

            assertTrue(exception.getMessage().contains("Vault is unavailable at [http://localhost:8200]"));
            assertTrue(exception.getMessage().contains("Connection could not be established: Connection refused"));
            assertInstanceOf(ResourceAccessException.class, exception.getCause());
        }

        @Test
        void throwsWhenVaultUnavailableWithoutEndpoint() {
            when(vaultTemplate.read("secret/app")).thenThrow(
                    new ResourceAccessException("Connection refused"));

            DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                    () -> vaultService.inject("{{secret/app.key}}"));

            assertTrue(exception.getMessage().startsWith("Vault is unavailable. Connection could not be established"));
            assertTrue(exception.getMessage().contains("Connection refused"));
            assertInstanceOf(ResourceAccessException.class, exception.getCause());
        }

        @Test
        void constructorWithConfigurationSetsVaultUri() {
            final GlobalTestConfiguration config = new GlobalTestConfiguration();
            final Vault vault = new Vault();
            vault.setHost("vault.example.com");
            vault.setPort(8200);
            vault.setScheme("https");
            vault.setToken("my-token");
            config.setVault(vault);

            VaultService configuredService = new VaultService(config);
            assertNotNull(configuredService);
        }

        @Test
        void throwsWhenVaultNullInConfiguration() {
            final GlobalTestConfiguration config = new GlobalTestConfiguration();
            assertThrows(DefaultFrameworkException.class, () -> new VaultService(config));
        }
    }

    @Nested
    class VaultDtoTest {
        @Test
        void storesKeyAndValue() {
            final VaultService.VaultDto dto = new VaultService.VaultDto("k", "v");
            assertEquals("k", dto.getKey());
            assertEquals("v", dto.getValue());
        }
    }
}
